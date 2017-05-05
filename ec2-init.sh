#!/bin/bash
set -e

sudo apt install git
git clone -b s3 https://github.com/hopeng/acuman.git

acuman/support/all.sh
