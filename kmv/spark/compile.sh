#!/bin/bash

sbt clean assembly
mv target/scala-2.10/kmv-assembly-1.0.0.jar bin/kmv.jar

