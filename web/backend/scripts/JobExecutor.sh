#!/bin/bash

echo ${11}

response=$1
wd=$2
tool=$3

cd $wd
if [ $tool == "diamond" ]
then
  python3 $4 $5 $6 $7 $8
fi
if [ $tool == "bicon" ]
then
  python3 $4 $5 $6 $7 $8 $9
fi
if [ $tool == "trustrank" ] || [ $tool == "centrality" ]
then
  python3 $4 $5 $6 $7 $8 $9
fi

if [ $tool == "must" ]
then
  if [ -z "${12}" ]
  then
    java -jar $4 -nw $5 -s $6 -oe $7 -on $8 -hp $9 -mi ${10} -pd -ncd ${11}
  else
    java -jar $4 -nw $5 -s $6 -oe $7 -on $8 -hp $9 -mi ${10} -m -t ${11} -pd -ncd ${12}
  fi
fi

if [ $tool == 'domino' ]
then
  domino -a $4 -n $5 -s $6 -o $7
fi

if [ $tool == 'robust' ]
then
  python3.7 $4 $5 $6 $7 $8 $9 ${10} ${11}
fi

curl $response

