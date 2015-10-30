#!/bin/bash

MATSIZE=500
POW=4
for np in 2 4
	do
		for nb in 4
			do
				echo "Slaves = $np / # of blocks = $nb"
				RunKMV.sh $np $MATSIZE $nb $POW &
			done
	done

echo "Done sweep"
