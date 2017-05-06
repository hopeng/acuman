#!/bin/bash
set -e

basedir=`dirname "$0"`

cd $basedir
git pull

$basedir/package.sh

sudo systemctl restart acuman