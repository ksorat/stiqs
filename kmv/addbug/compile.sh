#!/bin/bash

sbt clean assembly
mv target/scala-2.10/addbug-assembly-1.0.0.jar addbug.jar

