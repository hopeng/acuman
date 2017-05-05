#!/bin/bash

basedir=`dirname "$0"`

sudo $basedir/setup-server.sh
source /etc/environment

$basedir/package.sh

sudo $basedir/install-service.sh
