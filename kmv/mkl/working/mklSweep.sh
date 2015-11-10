#!/bin/bash -l

#SBATCH
#SBATCH --job-name=Exp20k
#SBATCH --partition=parallel
#SBATCH --time=24:00:00
#SBATCH --nodes=1
#SBATCH --ntasks-per-node=1
#SBATCH --exclusive

export STUB="Test1k"
export SIZE=1000
export POW=10
export PNZ=0.5
export KMP_AFFINITY=verbose,granularity=core

conmv.sh $STUB $SIZE $PNZ

for i in `seq 0 4 24`;
	do
		
		export T=`printf %03d $i`
		export LOGFILE="Log.N$SIZE.Np$POW.T$T.txt"
		echo "Running with $i threads"
		echo "Saving lot to $LOGFILE"
		export OMP_NUM_THREADS=$i
		export MKL_NUM_THREADS=$i
		export MP_TASK_AFFINITY=core:$OMP_NUM_THREADS
		(time kmv.x $SIZE $STUB $POW) &> $LOGFILE
		head ${STUB}_Outvec.txt >> $LOGFILE
		tail ${STUB}_Outvec.txt >> $LOGFILE
		
	done

