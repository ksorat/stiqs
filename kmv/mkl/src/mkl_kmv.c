/* Reads input of matrix entries, approximates Exp(A)*v and writes output to file. */
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <omp.h>

#include "mkl.h"

#define INDEX( i, j, N) ( j+i*N )
int main( int argc, char *argv[] )
{
	double *A, *En, *Exp, *Ent, Aij;
	double *v, *Ev;
	int N, i, j, threads, Npow, n, incx, incy;	
	char line[256], FileInMat[256], FileInVec[256], FileOut[256];
	double SMALL=1.0e-16;
	double alpha, beta, start, stop, scl;
	FILE* file;
	alpha = 1.0;
	beta = 0.0;
	incx = 1;
	incy = 1;
	if (argc < 3) {
		printf("Format: mkl_kmv.x N Stubmat Npow\n");
		return -1;
	}
	N = atoi( argv[1] );
	Npow = atoi( argv[3] );
	sprintf(FileInMat, "%s_Matrix.txt", argv[2]);
	sprintf(FileInVec, "%s_Vector.txt", argv[2]);

	printf("Allocating memory for %d x %d matrices\n", N, N);

	A = (double *)mkl_malloc( N*N*sizeof(double), 64 );
	En = (double *)mkl_malloc( N*N*sizeof( double ), 64 );	
	Ent = (double *)mkl_malloc( N*N*sizeof( double ), 64 );		
	Exp = (double *)mkl_malloc( N*N*sizeof( double ), 64 );	
	v = (double *)mkl_malloc( N*sizeof( double), 64);
	Ev = (double *)mkl_malloc( N*sizeof( double), 64);

	if (A == NULL || En == NULL || Exp == NULL || Ent == NULL) {
		printf( "\n ERROR: Can't allocate memory for matrices. Aborting... \n\n");
		mkl_free(En);
		mkl_free(A);
		mkl_free(Exp);
		mkl_free(Ent);
		return 1;
	}

	//Initialize vectors/matrices
	for (i=0;i<N;i++) {
		v[i] = 0.0;
		Ev[i] = 0.0;
	}
	for (i=0;i<N;i++) {
		for (j=0;j<N;j++) {
			A[i,j] = 0.0;
			En[i,j] = 0.0;
			Ent[i,j] = 0.0;
			Exp[i,j] = 0.0;
		}
	}

	printf("Reading input matrix from %s\n", FileInMat);	
	file = fopen(FileInMat, "r");
	while ( fgets(line, sizeof(line), file) ) {
		sscanf(line, "%d\t%d\t%lf\n", &i, &j, &Aij);
		A[ INDEX(i,j,N) ] = Aij;
		En[ INDEX(i,j,N) ] = Aij;
	}
	fclose(file);

	printf("Reading input vector from %s\n", FileInVec);	
	file = fopen(FileInVec, "r");
	while ( fgets(line, sizeof(line), file) ) {
		sscanf(line, "%d\t%lf\n", &i, &Aij);
		
		v[i] = Aij;
	}
	fclose(file);	

	threads = omp_get_max_threads(); 
	mkl_set_num_threads(threads);
	printf("Performing computation w/ %d threads\n", threads);
	printf("\tNpow = %d\n", Npow);
	start = omp_get_wtime();
	//Initialize cumulative sum (Exp = I + A)
	for (i=0;i<N;i++) {
		Exp[ INDEX(i,i,N) ] = 1.0;
	} 

	// Exp = I
	cblas_daxpy(N*N, alpha, A, 1, Exp, 1); //Exp = I+A

	//En = A, set during read
	
	for (n=2;n<=Npow;n++) {
		//En = En*A/n
		scl = 1.0/n;
		
		//Ent = En*A/n (recursivity problem)
		//En = Ent	
		cblas_dgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans, N, N, N, scl, En, N, A, N, beta, Ent, N);
		cblas_dcopy(N*N, Ent, 1, En, 1);

		//Exp = Exp + En
		cblas_daxpy(N*N, alpha, En, 1, Exp, 1);
	}
	//Now, calculate Exp*v = Ev
	cblas_dgemv(CblasRowMajor, CblasNoTrans, N, N, alpha , Exp, N, v, 1, beta, Ev, 1);
	
	stop = omp_get_wtime() - start;
	printf("\tComputation took %f seconds.\n", stop);
	printf("Writing output w/ tolerance %2.2e\n", SMALL);
	sprintf(FileOut, "%s_Outvec.txt", argv[2]);

	//Write output vector
	file = fopen(FileOut, "w");
	for (i=0;i<N;i++) {
			if ( fabs( Ev[i] ) > SMALL) {
				fprintf(file,"%d\t%lf\n", i,Ev[i]);
			}
	}

	fclose(file);

	//Clean up	
	mkl_free(En);
	mkl_free(A);
	mkl_free(Exp);
	mkl_free(Ent);
	mkl_free(v);
	mkl_free(Ev);

	return 0;
}

