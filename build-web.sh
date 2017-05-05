#!/bin/bash
set -e

# build web
cd static
npm install
bower install
grunt
# todo upload to s3://acuman-web
aws cp dist/* s3://acuman-web/
cd -
