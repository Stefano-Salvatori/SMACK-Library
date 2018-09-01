#!/bin/bash
export DEBIAN_FRONTEND=noninteractive
#setup spark
sudo mkdir -p /tmp/spark-events

#install java and scala
echo "Installing Java..."
sudo apt-get update
sudo apt-get install default-jdk -y
sudo apt-get remove scala-library scala -y
sudo wget http://scala-lang.org/files/archive/scala-2.12.4.deb
sudo dpkg -i scala-2.12.4.deb -y

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
