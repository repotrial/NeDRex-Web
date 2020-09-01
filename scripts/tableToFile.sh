#!/bin/sh
file=$2/$3.list
printf "#" > $file
echo "$1/executeSQLCommand.sh \"$4\" >> $file"
`$1/executeSQLCommand.sh "$4" >> $file`
