#!/bin/bash
MATSIZE="$1"
NZMAT="$2"
NUMBLOCKS="$3"
POW="$4"

MATSTUB="Prod"
EHADOOP="/root/ephemeral-hdfs/bin/hadoop"
PHADOOP="/root/persistent-hdfs/bin/hadoop"

MASTERIP=`cat spark-ec2/masters`
DATADIR="/mnt/Data" #Large partition to hold data
echo "Setting up run on master ..."

#Create data
mkdir $DATADIR
mv /working .
~/working/genMat.py $MATSIZE 1 $NZMAT $DATADIR/${MATSTUB}_Vector.txt
~/working/genMat.py $MATSIZE $NZMAT $DATADIR/${MATSTUB}_Matrix.txt

echo "Copying to hadoop ..."
$EHADOOP fs -mkdir /user
$EHADOOP fs -mkdir /user/root

$EHADOOP fs -put $DATADIR/${MATSTUB}_Vector.txt /user/root/
$EHADOOP fs -put $DATADIR/${MATSTUB}_Matrix.txt /user/root/

$EHADOOP fs -ls /user/root/

(time spark/bin/spark-submit --class apl.stiqs.runkmv --master spark://$MASTERIP:7077 ~/working/kmv.jar $MATSTUB $POW $NUMBLOCKS &> Log.txt ) &> Time.txt

echo "-/--------------/-" >>  Log.txt
cat Time.txt >> Log.txt
echo "-/--------------/-" >>  Log.txt

ls -lh $DATADIR/ >> Log.txt
ls -l  $DATADIR/ >> Log.txt
echo "-/--------------/-" >>  Log.txt
$EHADOOP fs -ls /user/root/Outvec_${MATSTUB}/ >> Log.txt
$EHADOOP fs -cat /user/root/Outvec_${MATSTUB}/part* | head >> Log.txt
echo "-/--------------/-" >>  Log.txt
$EHADOOP fs -cat /user/root/Outvec_${MATSTUB}/part* | tail >> Log.txt
echo "-/--------------/-" >>  Log.txt
echo "Matrix size = $MATSIZE" >> Log.txt
echo "Density = $NZMAT" >> Log.txt
echo "Block decomposition = $NUMBLOCKS" >> Log.txt
echo "Npow = $POW" >> Log.txt





