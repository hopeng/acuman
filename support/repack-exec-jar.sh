#!/bin/bash

baseDir=$(dirname "$0")

jarName=$1

echo "repacking jar $jarName"
cat $baseDir/boot.sh $1 > tmpjar && mv tmpjar $1
chmod +x $1
