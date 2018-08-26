#!/bin/bash
sudo service mesos-master stop
sudo service mesos-slave stop
sudo service marathon stop
sudo service zookeeper stop
sudo mkdir -p /tmp/spark-events
my_ip=`ip route get 8.8.8.8 | awk '{print $NF; exit}'`
my_id=1
i=0;
#start zookeeper config string
echo -n "zk://" > /etc/mesos/zk

for var in "$@"
do
    echo -n "$var:2181," >> /etc/mesos/zk
    i=$((i + 1))
    if [ $my_ip = $var ]; then
       my_id=$i;
    fi
    echo "server.$i=$var:2888:3888" >> /etc/zookeeper/conf/zoo.cfg
done

#remove last comma before closing zookeeper config string
truncate -s-1 /etc/mesos/zk
echo "/mesos" >> /etc/mesos/zk

echo $my_id > /etc/zookeeper/conf/myid
quorum=$(($# / 2 + 1))
sed -i -e "s/1/$quorum/g" /etc/mesos-master/quorum
echo MESOS_QUORUM=$quorum >> /etc/default/mesos-master

echo $my_ip > /etc/mesos-master/ip
sudo cp /etc/mesos-master/ip /etc/mesos-master/hostname
echo "Starting mesos master on $my_ip"
sudo hostname $my_ip
sudo service zookeeper start
sudo service mesos-master start