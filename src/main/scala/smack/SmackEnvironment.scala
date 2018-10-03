package smack

import cluster.MesosCluster
import net.liftweb.json.JsonAST.{JField, JObject, JString, JValue}
import net.liftweb.json.parse
import smack.CassandraCluster.CassandraConnectionInfo
import smack.KafkaCluster.KafkaConnectionInfo
import task.TaskBuilder

import scala.language.postfixOps

object SmackEnvironment {
  val SPARK_VERSION = "spark-2.3.1-bin-hadoop2.7"
}

/**
  * @param mesos
  * the mesos cluster that 'contains the stack'
  * @param cassandraClusterName
  * the name of the cassandra cluster (this will be used also to
  * identify cassandra tasks in the cluster)
  * @param kafkaClusterName
  * the name of the kafka cluster (this will be used to identify kafka
  * tasks in the cluster)
  */
class SmackEnvironment(private val mesos: MesosCluster,
                       private var cassandraClusterName: String,
                       private var kafkaClusterName: String) {
  private val cassandraCluster: CassandraCluster =
    new CassandraCluster(mesos, cassandraClusterName.toLowerCase)
  private val kafkaCluster: KafkaCluster =
    new KafkaCluster(mesos, kafkaClusterName.toLowerCase)

  private val sparkDispathcerTask = new TaskBuilder()
    .setId("spark-dispatcher")
    .setCpus(0.5).setMemory(512).setDisk(0)
    .setCmd(s"/root/${SmackEnvironment.SPARK_VERSION}/bin/spark-class " +
      "org.apache.spark.deploy.mesos.MesosClusterDispatcher " +
      s"--master mesos://${mesos.zkConnectionString}/mesos")
    .build()


  /**
    * Starts Cassandra on mesos.
    *
    * @param serversCount
    * number of cassandra nodes
    * @param cpus
    * cpu reserved for each node
    * @param memory
    * memory for each node
    */
  def startCassandraCluster(serversCount: Int, cpus: Double, memory: Double)
  : Unit = this.cassandraCluster.start(serversCount, cpus, memory)


  /**
    * Starts Kafka on mesos
    *
    * @param brokersCount
    * number of kafka brokers
    * @param cpus
    * cpus for each broker
    * @param memory
    * memory for each broker
    */
  def startKafkaCluster(brokersCount: Int, cpus: Double, memory: Double): Unit = {
    this.kafkaCluster.start(brokersCount, cpus, memory)
  }

  /**
    * Adds a node in the existing cassandra cluster
    */
  def addCassandraNodes(howMany: Int): Unit = {
    this.cassandraCluster.addNodes(howMany)
  }

  /**
    * Adds a kafka broker in the existing kafka cluster
    */
  def addKafkaBrokers(howMany: Int): Unit = {
    this.kafkaCluster.addNodes(howMany)

  }

  /**
    * Start te spark framework on mesos to allow launching spark jobs in the cluster.
    *
    */
  def startSparkFramework(): Unit = {
    mesos.run(this.sparkDispathcerTask)
  }


  /**
    * Get the information about the cassandra cluster:
    * cluster name, nodes count, ip addresses of the nodes and the port to use for
    * the connection
    *
    * @return
    * An optional that contains the informations of the cassandra cluster;
    * None if the cluster doesn't exist
    */
  def getCassandraConnectionInfo: Option[CassandraConnectionInfo] = {
    this.cassandraCluster.getConnectionInfo
  }

  /**
    * Get the informations about the kafka cluster:
    * cluster name, brokers count, and a map that associates ip of the broker to the port they
    * are listening on
    *
    * @return
    * An optional that contains the informations of the kafka cluster;
    * None if the cluster doesn't exist
    */
  def getKafkaConnectionInfo: Option[KafkaConnectionInfo] = {
    this.kafkaCluster.getConnectionInfo
  }

  /**
    *
    * @return the ip address of the spark dispatcher thread
    */
  def getSparkDispatcherIp: Option[String] = {
    this.mesos.getTaskInfo(this.sparkDispathcerTask) match {
      case Some(info) =>
        val tasks = parse(info)
        val dispatcherIp = for {JObject(task) <- tasks
                                JField("host", JString(ip)) <- task} yield ip
        Some(dispatcherIp(0))
      case None => None
    }

  }


}
