#!/bin/bash

#install spark
echo "Getting Spark package..."
file="spark-2.3.1-bin-hadoop2.7.tgz"
if [ -f "$file" ]
then
	echo "$file alredy installed"
else
	wget http://mirror.nohup.it/apache/spark/spark-2.3.1/$file
	tar -xzvf $file

fi

#install java
echo "Installing Java..."
sudo apt-get update
sudo apt-get install default-jdk -y

#install mesosphere package
#mesos, marathon, chronos zookeeper...
echo "Installing mesos core components(mesos, marathon, chronos, zookeeper...)"
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list
sudo apt-get -y update
sudo apt-get install mesos -y
sudo apt-get install marathon -y
sudo apt-get install chronos -y