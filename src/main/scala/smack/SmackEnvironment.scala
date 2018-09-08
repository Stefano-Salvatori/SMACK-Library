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

class SmackEnvironment(private val mesos: MesosCluster) {


  private var cassandraClusterName: String = ""
  private var cassandraNodesCount = 0
  private var kafkaBrokersCount = 0

  /**
    * Start a cassandra database with the configurations (name,nodes count..) specified
    * in this smack environment object
    */
  def startCassandraDatabase(clusterName: String, serversCount: Int, cpus: Double, memory: Double)
  : Unit = {
    this.cassandraClusterName = clusterName
    runCassandraNode(cpus, memory, serversCount)
    this.cassandraNodesCount += serversCount
  }

  /**
    * Start a kafka cluster with the configurations (brokers count...) specified
    * in this smack environment object
    */
  def startKafkaCluster(brokersCount: Int, cpus: Double, memory: Double): Unit = {
    this.runKafkaBroker(cpus, memory, brokersCount)
    this.kafkaBrokersCount += brokersCount
  }

  /**
    * Adds a node in the cassandra cluster
    */
  def addCassandraNode(cpus: Double, memory: Double): Unit = {
    this.cassandraNodesCount += 1
    runCassandraNode(cpus, memory)
  }

  /**
    * Adds a kafka broker
    */
  def addKafkaBroker(cpus: Double, memory: Double): Unit = {
    this.kafkaBrokersCount += 1
    runKafkaBroker(cpus, memory)
  }

  /**
    * Start te spark framework on mesos to allow launching spark jobs in the cluster.
    *
    */
  def startSparkFramework(): Unit = {
    val sparkDispatcherTask = new TaskBuilder()
      .setId("spark-dispatcher")
      .setCpus(0.5).setMemory(512).setDisk(0)
      .setCmd(s"/root/${SmackEnvironment.SPARK_VERSION}/bin/spark-class " +
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

  private def runCassandraNode(cpus: Double, memory: Double, instances: Int = 1)
  : Unit = {
    val task = new CassandraTask(s"cassandra-${UUID.randomUUID}", cpus, memory, 0, None, instances)
    task.set(CassandraVariable.CASSANDRA_CLUSTER_NAME, this.cassandraClusterName)
    task.set(CassandraVariable.CASSANDRA_SEEDS, mesos.agents.map(_.getIp).mkString(","))
    mesos.run(task)
  }

  private def runKafkaBroker(cpus: Double, memory: Double, instances: Int = 1) = {
    val kafka = new KafkaTask(s"kafka-${UUID.randomUUID}", cpus, memory, disk = 0,
                               cmd = None, instances = instances)
    kafka.set(KafkaVariable.HOSTNAME_COMMAND,
               "ip -4 route get 8.8.8.8 | awk {'print $7'} | tr -d '\\n'")
    kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT,
               s"${mesos.masters.map(_.getIp).mkString(",")}:2181/kafka")
    kafka.set(KafkaVariable.KAFKA_LISTENERS, "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS, "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_LISTENER_SECURITY_PROTOCOL_MAP,
               "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT")
    kafka.set(KafkaVariable.KAFKA_INTER_BROKER_LISTENER_NAME, "INSIDE")
    kafka.setEnvVariable("KAFKA_DELETE_TOPIC_ENABLE", "true")

    mesos.run(kafka)
  }


}
