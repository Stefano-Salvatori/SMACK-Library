#!/bin/bash

MASTER=$1
i=$(($#-1))
CASSANDRA_INSTANCES=${!i}
i=$(($#))
KAFKA_INSTANCES=${!i}


#Start SPARK Framework in the cluster
ssh -t stefano@$MASTER 'sudo ./spark-2.3.0-bin-hadoop2.7/sbin/spark-daemon.sh start org.apache.spark.deploy.mesos.MesosClusterDispatcher 1 --host '$MASTER' --port 7077 --conf spark.driver.host='$MASTER' --master mesos://'$MASTER':5050'

#Start CASSANDRA on marathon
echo "{
  \"id\": \"cassandra-cluster\",
  \"groups\": [
    {
      \"id\": \"/cassandra-cluster\",
      \"apps\": [" > cassandra.json;
for (( i=1; i<=$CASSANDRA_INSTANCES; i++ ))
do
	echo -n "{
          \"id\": \"/cassandra-cluster/cassandra$i\",
          \"container\": {
            \"docker\": {
              \"image\": \"cassandra:latest\",
              \"network\": \"HOST\",
              \"ports\": [
                7199,
                7000,
                7001,
                9160,
                9042
              ],
              \"privileged\": true,
              \"forcePullImage\": true,
              \"parameters\": [
                {
                  \"key\": \"env\",
                  \"value\": \"CASSANDRA_CLUSTERNAME=cassandra-cluster\"
                },
                {
                  \"key\": \"env\",
                  \"value\": \"CASSANDRA_SEEDS = " >> cassandra.json
				k=0
				for var in "$@"
				do
					if [ $k -ne 0 ] && [ $(($#-2)) -gt $k ]; then
						echo -n $var >> cassandra.json
						if [ $k != $(($#-3)) ]; then
							echo -n "," >> cassandra.json
						fi
					fi
					k=$(($k+1))
				done
	echo " \"
                }
              ]
            }
          },
          \"cpus\": 1,
          \"mem\": 2048,
          \"instances\": 1
        }" >> cassandra.json;
		if [ $i != $CASSANDRA_INSTANCES ]; then
			echo "," >> cassandra.json;
		fi
done
echo " ]
    }
  ]
}" >> cassandra.json
curl -X POST -H "Content-type: application/json" $MASTER:8080/v2/groups?force=true -d@cassandra.json


#Start KAFKA on marathon
for (( i=1; i<=$KAFKA_INSTANCES; i++ ))
do
portOutside=$((9095-$i))
portInside=$((9093-$i))
echo "
{
		\"id\": \"kafka$i\",
		  \"container\": {
			\"docker\": {
			  \"image\": \"wurstmeister/kafka\",
			   \"network\": \"HOST\",
			  \"forcePullImage\": true,
			 \"privileged\": true,
			 \"parameters\": [

				 {
					\"key\": \"env\",
					\"value\": \"KAFKA_ZOOKEEPER_CONNECT=$MASTER:2181/kafka\"
				 },

				{
					\"key\": \"env\",
					\"value\": \"HOSTNAME_COMMAND=ip route get 8.8.8.8 | awk '{print $NF; exit}'\"
				 },
				 {
					\"key\": \"env\",
					\"value\": \"KAFKA_ADVERTISED_LISTENERS=INSIDE://:$portInside,OUTSIDE://:$portOutside\"
				 },
				 {
					\"key\": \"env\",
					\"value\": \"KAFKA_LISTENERS=INSIDE://:$portInside,OUTSIDE://:$portOutside\"
				 },
				  {
					\"key\": \"env\",
					\"value\": \"KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT\"
				 },
				  {
					\"key\": \"env\",
					\"value\": \"KAFKA_INTER_BROKER_LISTENER_NAME=INSIDE\"
				 }
				]
				}
			 },
			 \"cpus\": 2,
			 \"mem\": 2048,
			 \"instances\": 1
		}
" > kafka.json
curl -X POST -H "Content-type: application/json" $MASTER:8080/v2/apps -d@kafka.json
done
