#!/bin/sh
sleep 1
echo 'db.jobDoc.insert({writeConcern: {id: "'$2'", doc: "altered Document '$1'"}})'
mongo -u scaper -p repo --eval 'db.jobDoc.save({_id: "'$2'", _class: "de.exbio.reposcapeweb.db.JobDoc", doc: "altered Document '$1'"})' reposcape
curl 'http://localhost:8090/backend/finishedJob?j='$2
