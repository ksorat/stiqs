#!/bin/bash

MATSIZE=20000
POW=8
for np in 2 4 8 16 32 
	do
		for nb in 4 8 16
			do
				echo "Slaves = $np / # of blocks = $nb"
				RunKMV.sh $np $MATSIZE $nb $POW &
			done
	done

echo "Done sweep"