#!/bin/bash
sed -i "s/Connector port=\"8080\"/Connector port=\"$PORT\"/g" $1
sed -i "s/unpackWARs=\"true\"/unpackWARs=\"false\"/g" $1
sed -i "s/autoDeploy=\"true\"/autoDeploy=\"false\"/g" $1
