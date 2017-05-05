#!/bin/bash

git clone -b s3 https://github.com/hopeng/acuman.git
cd acuman

git pull
../gradlew clean build
cd static
grunt
cd -
