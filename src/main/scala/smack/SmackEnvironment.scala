package smack

import java.util.UUID

import cluster.MesosCluster
import com.datastax.driver.core.{Cluster, Session}
import task.{CassandraTask, GenericTask, KafkaTask, TaskBuilder}
import task.CassandraTask.CassandraVariable
import task.KafkaTask.KafkaVariable

import scala.util.Random

object SmackEnvironment {
  val SPARK_VERSION = "spark-2.3.1-bin-hadoop2.7"
}

class SmackEnvironment(private val mesos: MesosCluster,
                       private var cassandraDatabaseName: String,
                       private var cassandraNodesCount: Int,
                       private var kafkaBrokersCount: Int) {


  /**
    * Start a cassandra database with the configurations (name,nodes count..) specified
    * in this smack environment object
    */
  def startCassandraDatabase(): Unit = {
    for (c <- 1 to this.cassandraNodesCount) {
      runCassandraNodeContainer()
    }
  }

  /**
    * Start a kafka cluster with the configurations (brokers count...) specified
    * in this smack environment object
    */
  def startKafkaCluster(): Unit = {
    val kafka = new KafkaTask(s"kafka-${UUID.randomUUID}", cpus = 2, mem = 2048, disk = 1024,
                               cmd = None, this.kafkaBrokersCount)
    kafka.set(KafkaVariable.HOSTNAME_COMMAND,
               "ip -4 route get 8.8.8.8 | awk {'print $7'} | tr -d '\\n'")
    kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT,
               s"${mesos.masters.map(_.getIp).mkString(",")}:2181/kafka")
    kafka.set(KafkaVariable.KAFKA_LISTENERS, "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS, "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_LISTENER_SECURITY_PROTOCOL_MAP,
               "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT")
    kafka.set(KafkaVariable.KAFKA_INTER_BROKER_LISTENER_NAME, "INSIDE")
    kafka.setEnvVariable("KAFKA_DELETE_TOPIC_ENABLE","true")

    mesos.run(kafka)
  }

  /**
    * Adds a node in the cassandra cluster
    */
  def addCassandraNode(): Unit = {
    this.cassandraNodesCount += 1
    runCassandraNodeContainer()
  }

  /**
    * Adds a kafka broker
    */
  def addKafkaBroker(): Unit = {
    this.kafkaBrokersCount += 1
    runKafkaBrokerContainer()
  }

  /**
    * Start te spark framework on mesos to allow launching spark jobs in the cluster.
    *
    */
  def runSparkFramework(): Unit = {
    val sparkDispatcherTask = new TaskBuilder()
      .id("spark-dispatcher")
      .cpus(0.5).mem(512).disk(0)
      .cmd(s"/root/${SmackEnvironment.SPARK_VERSION}/bin/spark-class " +
        "org.apache.spark.deploy.mesos.MesosClusterDispatcher " +
        s"--master mesos://${mesos.zkConnectionString}/mesos")
      .build()
    mesos.run(sparkDispatcherTask)


  }


  def getCassandraSession: Session = {
    val builder = new Cluster.Builder
    mesos.agents.foreach(a => builder.addContactPoint(a.getIp))
    val cluster = builder
      .withPort(9042)
      .build()
    cluster.connect()
  }

  private def runCassandraNodeContainer(): Unit = {
    val task = new CassandraTask(s"cassandra-${UUID.randomUUID}", 2, 2048, 0, None)
    task.set(CassandraVariable.CASSANDRA_CLUSTER_NAME, this.cassandraDatabaseName)
    task.set(CassandraVariable.CASSANDRA_SEEDS, mesos.agents.map(_.getIp).mkString(","))
    mesos.run(task)
  }

  private def runKafkaBrokerContainer(): Unit = {
    val kafka = new KafkaTask(s"kafka-${UUID.randomUUID}", cpus = 2, mem = 2048, disk = 1024,
                               cmd = None)
    kafka.set(KafkaVariable.HOSTNAME_COMMAND,
               "ip -4 route get 8.8.8.8 | awk {'print $7'} | tr -d '\\n'")
    kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT,
               s"${mesos.masters.map(_.getIp).mkString(",")}:2181/kafka")
    kafka.set(KafkaVariable.KAFKA_LISTENERS, "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS, "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_LISTENER_SECURITY_PROTOCOL_MAP,
               "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT")
    kafka.set(KafkaVariable.KAFKA_INTER_BROKER_LISTENER_NAME, "INSIDE")

    mesos.run(kafka)
  }


}
