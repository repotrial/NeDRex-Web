#!/bin/bash

cd $1
mkdir diamond
cd diamond
wget https://github.com/dinaghiassian/DIAMOnD/archive/master.zip
unzip master.zip
rm master.zip
mv DIAMOnD-master/* ./
rm -rf DIAMOnD-master
rm -rf Example
rm README.md