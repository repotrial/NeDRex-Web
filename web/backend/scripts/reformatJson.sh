#!/bin/bash

sed -i s/},{\"/},\\n{\"/g $1
sed -i 's@\\r@@g' $1