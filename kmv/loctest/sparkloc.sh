#!/bin/bash

spark-submit --class apl.stiqs.runkmsq --master local[2] kmv.jar Large 2 &> Log.txt
