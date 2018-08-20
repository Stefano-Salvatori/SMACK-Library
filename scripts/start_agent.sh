#!/bin/bash

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
echo $my_ip > /etc/mesos-slave/ip
sudo cp /etc/mesos-slave/ip /etc/mesos-slave/hostname
sudo echo 'docker,mesos' > /etc/mesos-slave/containerizers
sudo echo '10mins' > /etc/mesos-slave/executor_registration_timeout
sudo rm -f /var/lib/mesos/meta/slaves/latest
echo "Starting mesos agent on $my_ip"
sudo service mesos-slave start