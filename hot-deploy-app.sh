#!/bin/bash
set -e

basedir=`dirname "$0"`

$basedir/support/package.sh

sudo systemctl restart acuman