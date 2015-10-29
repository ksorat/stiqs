package apl.stiqs

//var Stub = "Big"
//var Npow = 2
//var Nbx = 4

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.{SparkConf, SparkContext}

object runkmv {
    //Defining own block matrix add because of weird behavior in Spark default
    //Note, this is not optimal and should be replaced
  def AddBM( A: BlockMatrix , B: BlockMatrix ): BlockMatrix = {

    //Assume both matrices have same decomposition
    var Rpb = A.rowsPerBlock
    var Cpb = A.colsPerBlock

    //Peel to entries
    var Aijs = A.toCoordinateMatrix.entries
    var Bijs = B.toCoordinateMatrix.entries

    //Add via identifying same ij's
    var ABijs = Aijs.union(Bijs)
    var Tuples = ABijs.map{ ABij => ( ( ABij.i, ABij.j ), ABij.value )}
    var RedTuples = Tuples.reduceByKey(_ + _)
    var Sijs = RedTuples.map{ Sij => MatrixEntry( Sij._1._1, Sij._1._2, Sij._2)}
    var Scm = new CoordinateMatrix(Sijs)
    Scm.toBlockMatrix(Rpb,Cpb)

  }
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

    val Ments = raw.map{ line => line.split("\t")}.map{ ijA => MatrixEntry( ijA(0).toLong, ijA(1).toLong, ijA(2).toDouble)}
    //var nzents = ents.filter{ anent => anent.value != 0 } // Pull zeros

    val inMatcm = new CoordinateMatrix(Ments)
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
    val Vents = raw.map{ line => line.split("\t")}.map{ iA => MatrixEntry( iA(0).toLong, 0, iA(1).toDouble)}
    val inVeccm = new CoordinateMatrix(Vents,Nr,Nc)
    val inVec = inVeccm.toBlockMatrix(Rpb,1)

    //Construct identity matrix
    val diags = sc.parallelize(0 to Nr-1)
    val Eyecm = new CoordinateMatrix( diags.map{ d => MatrixEntry(d, d, 1.0)},Nr,Nc )
    val Eye = Eyecm.toBlockMatrix(Rpb,Cpb)
    //Do a bunch of matrix multiplies to spend time calculating
    //Calculate consecutive terms in matrix exponent
    //Ie, exp(A) = sum_i [ A^{n}/n!]
    // exp(A) = sum_i [ M_n ], M_1 = A, M_{n+1} = M_{n}* A/(n+1)
    var Apow = inMat
    var Mexp = AddBM(inMat,Eye)
    
    for ( n <- 2 to Npow ) {
        //Scale A (origina matrix, held in Ments)
        var sclAcm = new CoordinateMatrix( Ments.map{ ijA => MatrixEntry( ijA.i, ijA.j, 1.0*ijA.value/n )})
        var sclA = sclAcm.toBlockMatrix(Rpb,Cpb)
        Apow = Apow.multiply(sclA)
        Mexp = AddBM(Mexp,Apow)
    }
    
    //Do matrix-vector multiply
    val outVec = Mexp.multiply(inVec)

    //Pull out entries from resultant vector
    val entsOut = outVec.toCoordinateMatrix().entries
    val outLines = entsOut.map{ anent => anent.i.toString + "\t" + anent.value.toString}

    //Save to disk and get outta here
    //Writing out compressed doesn't mess up partitioning if you want to use it
    //outLines.saveAsTextFile(FileOut,classOf[org.apache.hadoop.io.compress.GzipCodec])
    outLines.saveAsTextFile(FileOut)

   
   }
}
