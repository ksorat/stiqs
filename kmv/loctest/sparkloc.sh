#!/bin/bash
NUMPOW=4
NUMBLK=4
STUB="SweepTest"
spark-submit --class apl.stiqs.runkmv --master local[2] kmv.jar $STUB $NUMPOW $NUMBLK &> Log.txt
cat Outvec_$STUB/part* > ${STUB}_Outvec.txt

