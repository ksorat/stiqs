#!/usr/bin/python

import numpy as np
import sys

def genColumn(N,p):
	nz = np.random.random(N) #Is value non-zero
	nzval = np.random.random(N) #What is non-zero value?
	indr = range(N)
	ind = np.array(indr)

	Chk = (nz>=p)
	vals = nzval[Chk]
	ind = ind[Chk]

	return (ind,vals)	
def writeVector(N,p,file): #Writes vector of size N into file
	ind, nzval = genColumn(N,p)
	Nnz = ind.shape[0]
	outStr = ""
	for i in range(Nnz) :
		outStr = outStr + str( ind[i] ) + "\t" + str(nzval[i]) + "\n"

	file.write(outStr)

def writeMatrix(N,M,p,file): #Writes matrix of size NxM into file
	for i in range(N):
		ind, nzval = genColumn(M,p)
		Nnz = ind.shape[0]
		outStr = ""
		for j in range(Nnz):
			outStr = outStr + str(i) + "\t" + str(ind[j]) + "\t" + str(nzval[j]) + "\n"
		file.write(outStr)

numArg = len(sys.argv)

if (numArg == 4) :
	N = int(sys.argv[1])
	M = int(sys.argv[1])
	p = float(sys.argv[2])
	fileOut = sys.argv[3]
elif (numArg == 5) :
	N = int(sys.argv[1])
	M = int(sys.argv[2])
	p = float(sys.argv[3])
	fileOut = sys.argv[4]
else:
	print 'Incorrect call'
	print 'Format: genMat.py N (M) p Outfile'
	exit()


print 'Generating matrix ...'
print '\t', 'N = ', N
print '\t', 'M = ', M
print '\t', 'Nonzero probability = ', p
print '\t', 'Writing to ', fileOut



target = open(fileOut, 'w')
#Always have same entries for given size, but not the same seed for vec/mat
if (M == 1):
	np.random.seed(73313) 
	writeVector(N,p,target)
else:
	np.random.seed(31337) 
	writeMatrix(N,M,p,target)



target.close()







