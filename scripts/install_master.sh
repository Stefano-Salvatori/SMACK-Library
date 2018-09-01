#!/bin/bash
export DEBIAN_FRONTEND=noninteractive
#install spark
# echo "Installing Spark..."
#sudo mkdir -p /tmp/spark-events
#file="spark-2.3.1-bin-hadoop2.7.tgz"
#directory="spark-2.3.1-bin-hadoop2.7"
#if [ ! -f "$file" ]; then
#   echo "Getting spark package from http://mirror.nohup.it/apache/spark/spark-2.3.1/$file"
#   wget -4 http://mirror.nohup.it/apache/spark/spark-2.3.1/$file
#   echo "File downloaded!"
#else
#	echo "Spark package $file already exists"
#fi
#if [ ! -d "$directory" ]; then
#    echo "Extracting package into $directory..."
#    tar -xzvf $file
#else
#    echo "Spark directory $directory already exists"
#fi


#install java
echo "Installing Java..."
sudo apt-get update
sudo apt-get install default-jdk -y
sudo apt-get remove scala-library scala -y
sudo wget http://scala-lang.org/files/archive/scala-2.12.4.deb
sudo dpkg -i scala-2.12.4.deb -y

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