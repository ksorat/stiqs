#!/bin/bash
NUMPOW=5
NUMBLK=10
rm -rf Outvec_Big
spark-submit --class apl.stiqs.runkmv --master local[2] kmv.jar Big $NUMPOW $NUMBLK &> Log.txt
