#!/bin/bash
set -e

basedir=`dirname "$0"`/..

cd $basedir
git pull

# build jar
./gradlew clean build

# build web
cd static
npm install
bower install
grunt
cd -
