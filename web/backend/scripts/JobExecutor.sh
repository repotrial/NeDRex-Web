#!/bin/bash

echo ${11}

response=$1
wd=$2
tool=$3

cd $wd
if [ $tool == "diamond" ]
then
  $PYTHON $4 $5 $6 $7 $8
fi
if [ $tool == "bicon" ]
then
  $PYTHON $4 $5 $6 $7 $8 $9
fi
if [ $tool == "trustrank" ] || [ $tool == "centrality" ]
then
  $PYTHON $4 $5 $6 $7 $8 $9
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
  $4 -a $5 -n $6 -s $7 -o $8
fi

if [ $tool == 'robust' ]
then
  python3.7 $4 $5 $6 $7 $8 $9 ${10} ${11}
fi

if [ $tool == 'kpm' ]
then
  java -jar $4 -graphFile=$5 -matrix1=$6 -L1=$7 -K=$8 -removeBens
fi
curl $response

