#!/bin/bash
NUMPOW=20
NUMBLK=4
STUB="Test"
spark-submit --class apl.stiqs.runkmv --master local[2] kmv.jar $STUB $NUMPOW $NUMBLK &> Log.txt
cat Outvec_$STUB/part* > ${STUB}_Outvec.txt

