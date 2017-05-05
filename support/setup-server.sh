#!/bin/bash

set -e

apt -y install awscli

add-apt-repository -y ppa:webupd8team/java
apt-get update
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
apt -y install oracle-java8-installer

apt install git

apt -y install nodejs
ln -s /usr/bin/nodejs /usr/bin/node
apt -y install npm

npm install bower -g
npm install grunt-cli -g

# append this to /etc/environment
printf "\nJAVA_HOME=/usr/lib/jvm/default-java\n" >> /etc/environment
##################################
ln -s /usr/lib/jvm/java-8-oracle/ /usr/lib/jvm/default-java
sudo -u ubuntu -i source /etc/environment

#iptables -A PREROUTING -t nat -i eth0 -p tcp --dport 80 -j REDIRECT --to-port 4567
