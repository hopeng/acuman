#!/bin/bash
set -e

basedir=`dirname "$0"`

# build web
cd $basedir/static
npm install
bower install
grunt clean build
aws s3 rm s3://acuman-web/ --recursive
aws s3 cp dist/ s3://acuman-web/ --recursive
cd -
