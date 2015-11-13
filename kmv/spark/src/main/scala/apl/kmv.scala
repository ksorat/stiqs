package apl.stiqs

//var Stub = "Big"
//var Npow = 2
//var Nbx = 4

import org.apache.spark.mllib.linalg._
import org.apache.spark.mllib.linalg.distributed._
import org.apache.spark.{SparkConf, SparkContext}


object runkmv {
    def AddW( A: BlockMatrix, B: BlockMatrix): BlockMatrix = {
        //Wrapper for adding matrices
        // Either use my (slow) routine, built-in (broken) routine, or just return first arg
        //AddBM(A,B)
        //A.add(B)
        A
    }
    //Defining own block matrix add because of weird behavior in Spark default
    //Note, this is not optimal and should be replaced
  def AddBM( A: BlockMatrix , B: BlockMatrix ): BlockMatrix = {

    //Assume both matrices have same decomposition
    var Xpb = A.rowsPerBlock
    //var Cpb = A.colsPerBlock

    //Peel to entries
    var Aijs = A.toCoordinateMatrix.entries
    var Bijs = B.toCoordinateMatrix.entries

    //Add via identifying same ij's
    var ABijs = Aijs.union(Bijs)
    var Tuples = ABijs.map{ ABij => ( ( ABij.i, ABij.j ), ABij.value )}.reduceByKey(_ + _)
    var Sijs = Tuples.map{ Sij => MatrixEntry( Sij._1._1, Sij._1._2, Sij._2)}
    var Scm = new CoordinateMatrix(Sijs)
    Scm.toBlockMatrix(Xpb,Xpb)

  }
  def ScaleMatrix_l( A: BlockMatrix, d: Double, sc: SparkContext): BlockMatrix = {
    var Xpb = A.rowsPerBlock

    var Aents = A.toCoordinateMatrix().entries
    var SclAents = Aents.map{ent => new MatrixEntry( ent.i, ent.j, d*ent.value)}
    var SclAcm = new CoordinateMatrix(SclAents)
    SclAcm.toBlockMatrix(Xpb,Xpb)

  }
  def ScaleMatrix_L( A: BlockMatrix, d: Double, sc: SparkContext): BlockMatrix = {
    // Scale via multiplication by diagonal matrix
    var Xpb = A.rowsPerBlock
    

    A.multiply( GenDiag( A.numRows().toInt, d, Xpb, sc ) )

  }  
  def GenDiag( N: Int, d: Double, Xpb: Int, sc: SparkContext): BlockMatrix = {
    var diags = sc.parallelize(0 to N-1)
    var Lamcm = new CoordinateMatrix( diags.map{ i => MatrixEntry(i,i,d)}, N, N )
    Lamcm.toBlockMatrix(Xpb,Xpb)
  }
  def Unlazy( A: BlockMatrix ) {
    var nzents = A.toCoordinateMatrix().entries.count()
    var Nr = A.numRows()
    var Nc = A.numCols()
    println("Matrix density is " + 1.0*nzents/(Nc*Nr))
  }

  def WriteOut( A: BlockMatrix, Stub: String ) {
    val Nr = A.numRows()
    val Nc = A.numCols()
    var ents = A.toCoordinateMatrix().entries

    if (Nc == 1) {
        var FileOut = "Outvec_" + Stub
        var outLines = ents.map{ anent => anent.i.toString + "\t" + anent.value.toString}   
        outLines.saveAsTextFile(FileOut) 
    } else {
        var FileOut = "Outmat_" + Stub
        var outLines = ents.map{ anent => anent.i.toString + "\t" + anent.j.toString + "\t" + anent.value.toString}    
        //outLines.saveAsTextFile(FileOut)
        outLines.saveAsTextFile(FileOut,classOf[org.apache.hadoop.io.compress.GzipCodec])
    }
    //Save to disk and get outta here
    //Writing out compressed doesn't mess up partitioning if you want to use it
    //
    
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
    

    //Read matrix
    var raw = sc.textFile(MatFileIn) //Get raw data

    val Ments = raw.map{ line => line.split("\t")}.map{ ijA => MatrixEntry( ijA(0).toLong, ijA(1).toLong, ijA(2).toDouble)}
    //var nzents = ents.filter{ anent => anent.value != 0 } // Pull zeros

    val inMatcm = new CoordinateMatrix(Ments)
    val Nr = inMatcm.numRows().toInt
    val Nc = inMatcm.numCols().toInt
    val N = Nr //Assuming square matrix
    val Xpb = (N/Nbx).toInt //Rpb = Cpb
    
    println("kLog -/- Input matrix is size : ( " + Nr + " , " + Nc + " )")
    println("kLog -/- Raising input matrix to power : " + Npow)
    println("kLog -/- Using " + Nbx + " blocks in each dimension")
 
    //Convert to block matrix
    val inMat = inMatcm.toBlockMatrix(Xpb,Xpb)

    //Now read vector
    raw = sc.textFile(VecFileIn)
    val Vents = raw.map{ line => line.split("\t")}.map{ iA => MatrixEntry( iA(0).toLong, 0, iA(1).toDouble)}
    val inVeccm = new CoordinateMatrix(Vents,N,1)
    val inVec = inVeccm.toBlockMatrix(Xpb,1)

    //Do a bunch of matrix multiplies to spend time calculating
    //Calculate consecutive terms in matrix exponent
    //Ie, exp(A) = sum_i [ A^{n}/n!]
    // exp(A) = sum_i [ M_n ], M_1 = A, M_{n+1} = M_{n}* A/(n+1)
    var Apow = inMat
    var Mexp = AddW(Apow, GenDiag(N, 1.0, Xpb, sc)) //First two terms (0/1)
    inMat.cache()
    Apow.cache()    
    Mexp.cache()
    for ( n <- 2 to Npow ) {
        //Scale A (original matrix, held in Ments/inMat
        Apow = Apow.multiply( ScaleMatrix_L(inMat, 1.0/n, sc) ) 
        Mexp = AddW(Apow,Mexp)
    }
    
    //Do matrix-vector multiply / write data
    val outVec = Mexp.multiply(inVec)   
    WriteOut(outVec,Stub)

    //Just write matrix
    //WriteOut(Mexp,Stub)
   
   }
}
