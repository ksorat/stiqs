package apl.stiqs

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.{SparkConf, SparkContext}

object runAddBug {

  def main(args: Array[String]) {
    val appName = "addbug"
    val conf = new SparkConf().setAppName(appName)
    val sc = new SparkContext(conf)

    val N = 200
    val Nbx = 10

    val diags = sc.parallelize(0 to N-1)
    val Eyecm = new CoordinateMatrix( diags.map{ d => MatrixEntry(d, d, 1.0)},N,N)
    val diagsq = diags.cartesian(diags)
    val Bcm = new CoordinateMatrix( diagsq.map{ dd => MatrixEntry( dd._1, dd._2, 1.0*dd._1*dd._2)},N,N)
    

    val Rpb = (N/Nbx).toInt
    val Cpb = (N/Nbx).toInt

    var A = Eyecm.toBlockMatrix(Rpb,Cpb)
    var B = Bcm.toBlockMatrix(Rpb,Cpb)

    var C = A.add(B)

    val entsOut = C.toCoordinateMatrix().entries
    val outLines = entsOut.map{ anent => anent.i.toString + "\t" + anent.j.toString + "\t" + anent.value.toString}
    
    outLines.saveAsTextFile("Outdata")

   
   }
}
