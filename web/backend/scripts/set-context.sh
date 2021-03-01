#!/bin/bash
name=$(echo "$CONTEXT_PATH" | tr [/] [#])
if [ ${#name} -ge 2 ]; then name=${name:1:${#name}} ; else name="ROOT"; fi
echo $name
sed -i "s|Context path=\"/backend|Context path=\"$CONTEXT_PATH|g" $1
file=$(dirname $1)
mv $1 "$file/$name.xml"
