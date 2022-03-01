#!/bin/bash
diff $1 $2 > $3
sort -t" " -k2 $3 -o $3