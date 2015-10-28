#!/bin/bash
MATSIZE="$1"
NZMAT="$2"
NUMBLOCKS="$3"
MATSTUB="Mat"
EHADOOP="/root/ephemeral-hdfs/bin/hadoop"
PHADOOP="/root/persistent-hdfs/bin/hadoop"

MASTERIP=`cat spark-ec2/masters`
DATADIR="/mnt/Data"
echo "Setting up run on master ..."
#mkdir kbin
#export PATH="$PATH:~/kbin:."
#ln -s ~/ephemeral-hdfs/bin/hadoop ~/kbin/ehadoop
#ln -s ~/persistent-hdfs/bin/hadoop ~/kbin/phadoop
mkdir /mnt/Data
mv /working .
#~/working/genMat.py $MATSIZE $NZMAT ~/working/$MATSTUB.txt
~/working/genMat.py $MATSIZE $NZMAT /mnt/Data/$MATSTUB.txt
#gzip ~/working/$MATSTUB.txt

echo "Copying to hadoop ..."
$EHADOOP fs -mkdir /user
$EHADOOP fs -mkdir /user/root

#$EHADOOP fs -put ~/working/$MATSTUB.txt /user/root/
$EHADOOP fs -put /mnt/Data/$MATSTUB.txt /user/root/
$EHADOOP fs -ls /user/root/

(time spark/bin/spark-submit --class apl.stiqs.runkmsq --master spark://$MASTERIP:7077 ~/working/kmsq.jar $MATSTUB $NUMBLOCKS &> Log.txt ) &> Time.txt

echo "-/--------------/-" >>  Log.txt
cat Time.txt >> Log.txt
echo "-/--------------/-" >>  Log.txt
#ls -lh ~/working/$MATSTUB.txt >> Log.txt
#ls -l ~/working/$MATSTUB.txt >> Log.txt
ls -lh /mnt/Data/$MATSTUB.txt >> Log.txt
ls -l   /mnt/Data/$MATSTUB.txt >> Log.txt
echo "-/--------------/-" >>  Log.txt
$EHADOOP fs -ls /user/root/Sq_$MATSTUB/ >> Log.txt
echo "-/--------------/-" >>  Log.txt
echo "Matrix size = $MATSIZE" >> Log.txt
echo "Density = $NZMAT" >> Log.txt
echo "Block decomposition = $NUMBLOCKS" >> Log.txt




