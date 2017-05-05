#!/bin/bash
set -e

git pull
../gradlew clean build
cd static
grunt
cd -
