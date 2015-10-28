package apl.stiqs

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.{SparkConf, SparkContext}

object runkmsq {
  def main(args: Array[String]) {
    val appName = "kMSQ"
    val conf = new SparkConf().setAppName(appName)
    val sc = new SparkContext(conf)

    val Stub = args(0)
    //val FileIn = Stub + ".txt.gz"
    //For some reason, compressed input doesn't allow partitioning
    val FileIn = Stub + ".txt"
    val FileOut = "Sq_" + Stub 
    val Nbx = args(1).toInt // Number of blocks in each dimension

    val raw = sc.textFile(FileIn) //Get raw data
    val aijs = raw.map{ line => line.split("\t")} //Split into 3-tuples, i,j,Aij
    //Map 3-tuples into collection of MatrixEntry's
    val ents = aijs.map{ ijA => MatrixEntry( ijA(0).toLong, ijA(1).toLong, ijA(2).toDouble)}
    val nzents = ents.filter{ anent => anent.value != 0 } // Pull zeros
    val numnz = nzents.count()

    //Build distributed coordinate matrix, then convert to block matrix
    //Note, this is inefficient and should be improved
    val cMat = new CoordinateMatrix(nzents)

    val Nc = cMat.numCols().toInt
    val Nr = cMat.numRows().toInt
    val nzfrac = 1.0*numnz/(Nc*Nr)

    val Rpb = (Nr/Nbx).toInt
    val Cpb = (Nc/Nbx).toInt
    println("kLog -/- Input matrix is size : ( " + Nr + " , " + Nc + " )")
    println("kLog -/- Nonzero fraction = " + nzfrac )
    println("kLog -/- Using " + Nbx + " blocks in each dimension")
    val bMat = cMat.toBlockMatrix(Rpb, Cpb)

    //Calculate matrix square
    val bbMat = bMat.multiply(bMat)
    //val outDat = bbMat.blocks

    //Put back into coordinate matrix
    val cMatOut = bbMat.toCoordinateMatrix()
    //Peel out entries and prep for write-out
    val entsOut = cMat.entries // Only non-zero come out
    val outLines = entsOut.map{ anent => anent.i.toString + " " + anent.j.toString + " " + anent.value}
    //Writing out compressed data doesn't mess up partitioning
    outLines.saveAsTextFile(FileOut,classOf[org.apache.hadoop.io.compress.GzipCodec])
    //outLines.saveAsTextFile(FileOut)
   }
}
