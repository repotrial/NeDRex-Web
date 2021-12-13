#!/bin/bash
cat $1 | tail -c +2 | head -c -1 >> $2

if [ -z "${3}" ]
then
    printf ",\n" >> $2
else
  printf "]" >> $2
fi
