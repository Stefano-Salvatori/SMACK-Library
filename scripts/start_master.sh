#!/bin/bash

#start master
sudo service mesos-master stop
sudo service mesos-slave stop
sudo service marathon stop
sudo service zookeeper stop
sudo mkdir /tmp/spark-events
my_ip=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`
sudo hostname $my_ip
echo zk://$my_ip:2181/mesos > /etc/mesos/zk
sed -i -e "s/#server.1=.*:2888:3888/server.1=$my_ip:2888:3888/g" /etc/zookeeper/conf/zoo.cfg
echo MESOS_QUORUM=1 >> /etc/default/mesos-master
echo $my_ip > /etc/mesos-master/ip
sudo cp /etc/mesos-master/ip /etc/mesos-master/hostname
echo "Starting mesos master on $my_ip"
sudo service zookeeper start
sudo service mesos-master start