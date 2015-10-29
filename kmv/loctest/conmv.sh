!#/bin/bash

#Creates matrix/vector for testing
STUB=$1
N=$2
p=$3

genMat.py $N 1 $p ${STUB}_Vector.txt
genMat.py $N $p ${STUB}_Matrix.txt
