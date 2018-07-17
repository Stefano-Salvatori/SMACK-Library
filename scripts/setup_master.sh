#!/bin/bash
#install java8
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
sudo apt-get install oracle-java8-installer -y

#install mesosphere package
#mesos, marathon, chronos zookeeper...
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv E56151BF
DISTRO=$(lsb_release -is | tr '[:upper:]' '[:lower:]')
CODENAME=$(lsb_release -cs)
echo "deb http://repos.mesosphere.io/${DISTRO} ${CODENAME} main" | sudo tee /etc/apt/sources.list.d/mesosphere.list
sudo apt-get -y update
sudo apt-get install mesosphere -y

#start master
sudo stop mesos-master
sudo stop mesos-slave
sudo stop marathon
sudo stop zookeeper
sudo mkdir /tmp/spark-events
my_ip=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`
sudo hostname $my_ip
echo zk://$my_ip:2181/mesos > /etc/mesos/zk
#mkdir /etc/zookeeper/conf
#cp /etc/zookeeper/conf_example/zoo.cfg.dpkg-new /etc/zookeeper/conf/zoo.cfg
sed -i -e "s/#server.1=.*:2888:3888/server.1=$my_ip:2888:3888/g" /etc/zookeeper/conf/zoo.cfg
echo MESOS_QUORUM=1 >> /etc/default/mesos-master
echo $my_ip | sudo tee /etc/mesos-master/ip
sudo cp /etc/mesos-master/ip /etc/mesos-master/hostname
sudo start zookeeper
sudo start mesos-master