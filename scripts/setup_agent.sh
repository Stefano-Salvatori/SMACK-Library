#!/bin/bash
#install java
sudo apt-get update
sudo apt-get install default-jdk -y
#echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
#echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
#sudo add-apt-repository ppa:webupd8team/java -y
#sudo apt-get update
#sudo apt-get install oracle-java8-installer -y


#install mesos
#mesos, zookeeper
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list
sudo apt-get -y update
sudo apt-get install mesos -y

#install docker
sudo apt-get install apt-transport-https
sudo apt-get install ca-certificates
sudo apt-get install curl
sudo apt-get install software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -
sudo add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu  $(lsb_release -cs) stable"
sudo apt-get update
sudo apt-get install docker-ce -y

#start slave
sudo service mesos-master stop
sudo service mesos-slave stop
sudo service marathon stop
sudo service zookeeper stop
sudo mkdir /tmp/spark-events
my_ip=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`
master=$1
sudo hostname $my_ip
echo zk://$master:2181/mesos > /etc/mesos/zk
echo $my_ip | sudo tee /etc/mesos-slave/ip
sudo cp /etc/mesos-slave/ip /etc/mesos-slave/hostname
sudo echo 'docker,mesos' > /etc/mesos-slave/containerizers
sudo echo '10mins' > /etc/mesos-slave/executor_registration_timeout
sudo rm -f /var/lib/mesos/meta/slaves/latest
sudo service mesos-slave start