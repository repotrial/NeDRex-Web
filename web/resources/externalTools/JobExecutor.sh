#!/bin/bash

response=$1
wd=$2
tool=$3

cd $wd
if [ $tool == "diamond" ]
then
  python3 $4 $5 $6 $7
fi

curl $response

