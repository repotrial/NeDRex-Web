#!/bin/bash

if [ $configuration == "prod" ]; then
  sed -i "s|isProduction.*|isProduction = true|g" $1;
  sed -i "s|const HOST.*|const HOST = $BACKEND_HOST|g" $1
  sed -i "s|BACKEND_PORT =.*|BACKEND_URL =$BACKEND_PORT|g" $1
  sed -i "s|BACKEND_URL =.*|BACKEND_URL =$HOST:$BACKEND_PORT|g" $1

fi
