#!/bin/bash

response=$1
wd=$2
tool=$3

cd $wd
if [ $tool == "diamond" ]
then
  python3 $4 $5 $6 $7
fi
if [ $tool == "bicon" ]
then
  python3 $4 $5 $6 $7 $8 $9
fi
if [ $tool == "trustrank" ] || [ $tool == "centrality" ]
then
  python3 $4 $5 $6 $7
fi

if [ $tool == "must" ]
then
  java -jar $4 -nw $5 -s $6 -oe $7 -on $8
fi

curl $response

