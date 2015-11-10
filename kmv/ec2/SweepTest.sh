#!/bin/bash

MATSIZE=10000
POW=10
for np in 4 8
	do
		for nb in 8
			do
				echo "Slaves = $np / # of blocks = $nb"
				RunKMV.sh $np $MATSIZE $nb $POW &
			done
	done

echo "Done sweep"
