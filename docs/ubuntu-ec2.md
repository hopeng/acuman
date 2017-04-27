ssh -i hopeng.pem ubuntu@ec2-52-39-175-63.us-west-2.compute.amazonaws.com

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install git
sudo apt-get install oracle-java8-installer

# append this to /etc/environment
JAVA_HOME=/usr/lib/jvm/default-java
##################################
source /etc/environment
sudo ln -s /usr/lib/jvm/java-8-oracle/ /usr/lib/jvm/default-java

# reference
http://ajgupta.github.io/ubuntu/2014/09/18/Completely-uninstall-Java-from-Ubuntu-14.04/
http://tecadmin.net/install-oracle-java-8-jdk-8-ubuntu-via-ppa/




couchbase
wget http://packages.couchbase.com/releases/4.0.0/couchbase-server-community_4.0.0-ubuntu14.04_amd64.deb
http://ec2-52-33-209-111.us-west-2.compute.amazonaws.com:8091/


Please note that you have to update your firewall configuration to
allow connections to the following ports: 11211, 11210, 11209, 4369,
8091, 8092, 8093,9100 to 9105, 9998, 18091, 18092, 11214, 11215 and
from 21100 to 21299.

todo
use http://nginx.org/en/ to proxy jetty port to 80

