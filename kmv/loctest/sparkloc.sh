#!/bin/bash
NUMPOW=2
NUMBLK=4
STUB="Big"
#COMSTUB="spark-submit"
#COMSTUB="/Users/soratka1/Spark-1.5.0/bin/spark-submit --conf spark.io.compression.codec=lzf"
#COMSTUB="/Users/soratka1/Spark-1.5.1/bin/spark-submit"
COMSTUB="/Users/soratka1/spark-1.5.1-bin-hadoop2.4/bin/spark-submit"
cleanmv.sh $STUB
$COMSTUB --class apl.stiqs.runkmv --master local[4] kmv.jar $STUB $NUMPOW $NUMBLK &> Log.txt
#gunzip Outvec_$STUB/part*.gz
cat Outvec_$STUB/part* > ${STUB}_Outvec.txt

