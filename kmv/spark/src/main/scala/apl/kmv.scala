package apl.stiqs

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.{SparkConf, SparkContext}

object runkmv {
  def main(args: Array[String]) {
    val appName = "kMV"
    val conf = new SparkConf().setAppName(appName)
    val sc = new SparkContext(conf)

    // Usage: kmv.jar Stub Npow Nblks
    val Stub = args(0)
    val Npow = args(1).toInt // Number of times to multiply matrix
    val Nbx = args(2).toInt // Number of blocks in each dimension

    //For some reason, compressed input doesn't allow partitioning
    val MatFileIn = Stub + "_Matrix.txt"
    val VecFileIn = Stub + "_Vector.txt"
    val FileOut = "Outvec_" + Stub

    //Read matrix
    var raw = sc.textFile(MatFileIn) //Get raw data

    var ents = raw.map{ line => line.split("\t")}.map{ ijA => MatrixEntry( ijA(0).toLong, ijA(1).toLong, ijA(2).toDouble)}
    //var nzents = ents.filter{ anent => anent.value != 0 } // Pull zeros

    val inMatcm = new CoordinateMatrix(ents)
    val Nr = inMatcm.numRows().toInt
    val Nc = inMatcm.numCols().toInt
    val Rpb = (Nr/Nbx).toInt
    val Cpb = (Nr/Nbx).toInt
    println("kLog -/- Input matrix is size : ( " + Nr + " , " + Nc + " )")
    println("kLog -/- Raising input matrix to power : " + Npow)
    println("kLog -/- Using " + Nbx + " blocks in each dimension")
 
    //Convert to block matrix
    val inMat = inMatcm.toBlockMatrix(Rpb,Cpb)

    //Now read vector
    raw = sc.textFile(VecFileIn)
    ents = raw.map{ line => line.split("\t")}.map{ iA => MatrixEntry( iA(0).toLong, 0, iA(1).toDouble)}
    val inVeccm = new CoordinateMatrix(ents)
    val inVec = inVeccm.toBlockMatrix(Rpb,1)

    //Do a bunch of matrix multiplies to spend time calculating
    var Apow = inMat
    var Mexp = inMat
    val diags = sc.parallelize(0 to Nr-1)
    for ( n <- 2 to Npow ) {
        //Scale A
        var sclMatcm = new CoordinateMatrix( diags.map{ d => MatrixEntry( d, d, (1.0/n) ) } )
        var sclMat = sclMatcm.toBlockMatrix(Rpb,Cpb)
        var sclA = Apow.multiply(sclMat)
        Apow = Apow.multiply(sclA)

        Mexp = Mexp.add(Apow)
    }
    //Do matrix-vector multiply
    val outVec = Mexp.multiply(inVec)

    //Pull out entries from resultant vector
    val entsOut = outVec.toCoordinateMatrix().entries
    val outLines = entsOut.map{ anent => anent.i.toString + "\t" + anent.value}

    //Save to disk and get outta here
    //Writing out compressed doesn't mess up partitioning if you want to use it
    //outLines.saveAsTextFile(FileOut,classOf[org.apache.hadoop.io.compress.GzipCodec])
    outLines.saveAsTextFile(FileOut)

   
   }
}
