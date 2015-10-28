#!/bin/bash

#Set defaults
#NUMSLAVES="24"
#MATSIZE="50000" #Matrix is NxN
#NUMBLOCKS="10" #Number of blocks in each dimension
NUMSLAVES=$1
MATSIZE=$2
NUMBLOCKS=$3
POW=$4

NZMAT="0.5" #Non-zero probability

WORKDIR="/Users/soratka1/Work/stiqs/kmv/ec2/working"

STUB="N$MATSIZE.NB$NUMBLOCKS.S$NUMSLAVES.P$POW"
ID="kSpark.$STUB"

#Spawn cluster
echo "Spawning cluster $ID..."
~/ec2/spark-ec2 --deploy-root-dir=$WORKDIR -k spark-kareem -i ~/.ssh/spark-kareem.pem -s $NUMSLAVES launch $ID -z us-east-1c

#Save master IP
~/ec2/spark-ec2 get-master $ID | grep amazonaws > MasterIP.$STUB.txt
MASTERIP=`cat MasterIP.$STUB.txt`

echo "Spawn complete!"
echo "Master node is $MASTERIP"

echo "Prepping master ... "
ssh -o StrictHostKeyChecking=no -i ~/.ssh/spark-kareem.pem root@$MASTERIP 'bash -s' < remote.sh $MATSIZE $NZMAT $NUMBLOCKS $POW

echo "Run finished, shutting down."
scp -o StrictHostKeyChecking=no -i ~/.ssh/spark-kareem.pem root@$MASTERIP:~/Log.txt .
mv Log.txt Data/Log.$STUB.txt

echo "Killing cluster."
rm MasterIP.$STUB.txt
echo "y" | ~/ec2/spark-ec2 destroy $ID
