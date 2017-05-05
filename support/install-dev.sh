#!/bin/bash

cd static

# Once off
apt-get install npm

npm install bower -g
npm install grunt-cli -g

# build web
grunt