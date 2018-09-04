package smack

import cluster.MesosCluster
import task.{CassandraTask, GenericTask, KafkaTask}
import task.CassandraTask.CassandraVariable
import task.KafkaTask.KafkaVariable

import scala.util.Random

class SmackEnvironment(private val mesos: MesosCluster,
                       private var cassandraDatabaseName: String,
                       private var cassandraNodesCount: Int,
                       private var kafkaBrokersCount: Int) {


  def startCassandraDb(): Unit = {
    for (c <- 1 to this.cassandraNodesCount) {
      runCassandraNodeContainer()
    }
  }

  def startKafkaBrokers(): Unit = {
    for (k <- 1 to this.kafkaBrokersCount) {
      runKafkaBrokerContainer(k - 1)
    }
  }

  def addCassandraNode(): Unit = {
    this.cassandraNodesCount += 1
    runCassandraNodeContainer()
  }

  def runSparkFramework(): Unit = {
    mesos.run(new GenericTask("spark-dispathcer",
                               0.5,
                               512,
                               0,
                               Some("/root/spark-2.3.1-bin-hadoop2.7/bin/spark-class " +
                                 "org.apache.spark.deploy.mesos.MesosClusterDispatcher " +
                                 s"--master mesos://${mesos.getZkConnectionString}/mesos"),
                               None,
                               Map()))

  }

  private def runCassandraNodeContainer(): Unit = {
    val task = new CassandraTask(s"cassandra-${Random.nextInt(100)}", 2, 2048, 0, None)
    task.set(CassandraVariable.CASSANDRA_CLUSTER_NAME, this.cassandraDatabaseName)
    task.set(CassandraVariable.CASSANDRA_SEEDS, mesos.getAgents.map(_.getIp).mkString(","))
    mesos.run(task)
  }

  private def runKafkaBrokerContainer(k: Int): Unit = {
    //val insidePort = 9092 - k
    //val outsidePort = 9094 - k
    val kafka = new KafkaTask(s"kafka-${Random.nextInt(100)}", cpus = 2, mem = 2048, disk = 1024,
                               cmd = None)
    kafka.set(KafkaVariable.HOSTNAME_COMMAND,
               "${ip -4 route get 8.8.8.8 | awk {'print $7'} | tr -d '\\n'}")
    kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT, s"${mesos.getMasters.map(_.getIp).mkString(",")}:2181/kafka")
    kafka.set(KafkaVariable.KAFKA_LISTENERS, "PLAINTEXT://:9092")
    kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS, "PLAINTEXT://:9092")

    mesos.run(kafka)
  }


}
