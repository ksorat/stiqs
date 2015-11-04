#!/bin/bash
N=1000
Nb=10
#COMSTUB="spark-submit"
COMSTUB="/Users/soratka1/Spark-1.5.0/bin/spark-submit --conf spark.io.compression.codec=lzf"
spark-submit --class apl.stiqs.runAddBug --master local[2] addbug.jar $N $NB

