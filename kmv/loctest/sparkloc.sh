#!/bin/bash
NUMPOW=10
NUMBLK=20
STUB="Big"
COMSTUB="spark-submit"
#COMSTUB="/Users/soratka1/Spark-1.5.0/bin/spark-submit --conf spark.io.compression.codec=lzf"
spark-submit --class apl.stiqs.runkmv --master local[2] kmv.jar $STUB $NUMPOW $NUMBLK &> Log.txt
cat Outvec_$STUB/part* > ${STUB}_Outvec.txt

