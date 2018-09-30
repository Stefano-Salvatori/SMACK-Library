#!/bin/bash
export DEBIAN_FRONTEND=noninteractive
#install spark
 echo "Installing Spark..."
sudo mkdir -p /tmp/spark-events
file="spark-2.3.1-bin-hadoop2.7.tgz"
directory="spark-2.3.1-bin-hadoop2.7"
if [ ! -f "$file" ]; then
   echo "Getting spark package from http://mirror.nohup.it/apache/spark/spark-2.3.1/$file"
   curl http://mirror.nohup.it/apache/spark/spark-2.3.1/$file > $file
   echo "File downloaded!"
else
	echo "Spark package $file already exists"
fi
if [ ! -d "$directory" ]; then
    echo "Extracting package into $directory ..."
    tar -xzf $file
else
    echo "Spark directory $directory already exists"
fi

#install java and scala
scalaVersion=2.11.12
echo "Installing Java..."
apt-get update
apt-get install default-jdk -y
apt-get remove scala-library scala -y
wget --quiet http://scala-lang.org/files/archive/scala-$scalaVersion.deb
dpkg -i scala-$scalaVersion.deb

#install mesos
#mesos, zookeeper
echo "Installing mesos and zookeeper..."
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list
sudo apt-get update
sudo apt-get install mesos -y

#install docker
echo "Installing Docker..."
sudo apt-get install apt-transport-https
sudo apt-get install ca-certificates
sudo apt-get install curl
sudo apt-get install software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu  $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install docker-ce -y