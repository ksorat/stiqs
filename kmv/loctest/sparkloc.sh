#!/bin/bash

spark-submit --class apl.stiqs.runkmv --master local[2] kmv.jar Big 1 2 &> Log.txt
