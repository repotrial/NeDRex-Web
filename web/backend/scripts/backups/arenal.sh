#!/bin/bash

DB=docker_nedrex-db-volume
CACHE=docker_nedrex-cache-volume
USR=docker_nedrex-usr-volume

DB_NEW=${DB}_backup
CACHE_NEW=${CACHE}_backup
USR_NEW=${CACHE}_backup

echo "Removing old backups"
docker volume rm --name $DB_NEW
docker volume rm --name $CACHE_NEW
docker volume rm --name $USR_NEW

echo "Creating new backup volumes" 
docker volume create --name $DB_NEW
docker volume create --name $CACHE_NEW
docker volume create --name $USR_NEW

echo "Backing up DB"
docker container run -rm -it -v $DB:/from -v $DB_NEW:/to alpine ash -c 'cd /from ; cp -av . /to'

echo "Backing up CACHE" 
docker container run -rm -it -v $CACHE:/from -v $CACHE_NEW:/to alpine ash -c 'cd /from ; cp -av . /to'

echo "Backing up USR"
docker container run -rm -it -v $USR:/from -v $USR_NEW:/to alpine ash -c 'cd /from ; cp -av . /to'

echo "DONE"
