#!/bin/bash
set -e

basedir=`dirname "$0"`

cd $basedir
git pull

$basedir/support/package.sh

sudo systemctl restart acuman