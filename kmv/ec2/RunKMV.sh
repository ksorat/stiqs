#!/bin/bash

#Set defaults
#NUMSLAVES="24"
#MATSIZE="50000" #Matrix is NxN
#NUMBLOCKS="10" #Number of blocks in each dimension
NZMAT="0.5" #Non-zero probability

NUMSLAVES=$1
MATSIZE=$2
NUMBLOCKS=$3
POW=$4

#TYPE="m1.large"
TYPE="c3.large"
export PADSLAVES=`printf %03d $NUMSLAVES`
WORKDIR="/Users/soratka1/Work/stiqs/kmv/ec2/working" #Directory to be deployed to EC2 cluster
#SPEC2="/Users/soratka1/ec2/spark-ec2" #Location of spark-ec2 script
#SPEC2="/Users/soratka1/Spark-1.5.1/ec2/spark-ec2" #Location of spark-ec2 script
SPEC2="/Users/soratka1/spark-1.5.1-bin-hadoop2.4/ec2/spark-ec2"
STUB="N$MATSIZE.Np$POW.S$PADSLAVES.Nb$NUMBLOCKS"
ID="kSpark.$STUB"

#Spawn cluster
echo "Spawning cluster $ID ..."
$SPEC2 --deploy-root-dir=$WORKDIR -k spark-kareem -i ~/.ssh/spark-kareem.pem --no-ganglia -t $TYPE -s $NUMSLAVES launch $ID -z us-east-1c
echo "Finished spawning cluster $ID !"
#Save master IP
$SPEC2 get-master $ID | grep amazonaws > MasterIP.$STUB.txt
MASTERIP=`cat MasterIP.$STUB.txt`
echo "Master node for $ID is $MASTERIP"
#scp files from working because stupid deploy-root doesn't follow sym-links
scp -o StrictHostKeyChecking=no -i ~/.ssh/spark-kareem.pem $WORKDIR/*  root@$MASTERIP:/working/

echo "Prepping master node for $ID ... "
ssh -o StrictHostKeyChecking=no -i ~/.ssh/spark-kareem.pem root@$MASTERIP 'bash -s' < remote.sh $MATSIZE $NZMAT $NUMBLOCKS $POW

echo "Run $ID finished, shutting down."
scp -o StrictHostKeyChecking=no -i ~/.ssh/spark-kareem.pem root@$MASTERIP:~/Log.txt Log.$STUB.txt

mv Log.$STUB.txt Data/

echo "Killing cluster $ID !"
rm MasterIP.$STUB.txt
echo "y" | $SPEC2 destroy $ID

echo "Finished run $ID"
