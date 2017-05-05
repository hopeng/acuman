# The ec2 cloudformation init section
#!/bin/bash
set -e

git clone -b s3 https://github.com/hopeng/acuman.git

acuman/support/all.sh
