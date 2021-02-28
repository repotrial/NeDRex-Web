#!/bin/bash
sed -i "s/Context path=\"backend\"/Context path=/\"$CONTEXT_PATH\"/g" $1
