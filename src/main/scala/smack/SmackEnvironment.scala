package smack

import cluster.MesosCluster
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


  /*case class FullTaskResponse(id: String,
                              backoffFactor: Double,
                              backoffSeconds: Double,
                              cmd: String,
                              cpus: Double,
                              disk: Double,
                              executor: String,
                              instances: Int,
                              labels: Map[String, String],
                              maxLaunchDelaySeconds: Int,
                              mem: Double,
                              gpus: Double,
                              networks: Array[Map[String, String]],
                              portDefinitions: Array[(String, String, String)], //port:-,name:-,protocol:-,
                              requirePorts: Boolean,
                              upgradeStrategy: Map[String, Int],
                              version: String,
                              versionInfo: Map[String, String],
                              killSelection: String,
                              unreachableStrategy: Map[String, Int],
                              tasksStaged: Int,
                              tasksRunning: Int,
                              tasksHealthy: Int,
                              tasksUnhealthy: Int,
                              deployments: Array[Map[String, String]],
                              tasks: Array[String])*/
  //cassandraClusterName = cassandraClusterName.toLowerCase()
  //kafkaClusterName = kafkaClusterName.toLowerCase()

  private val cassandraCluster: CassandraCluster =
    new CassandraCluster(mesos, cassandraClusterName.toLowerCase)
  private val kafkaCluster: KafkaCluster =
    new KafkaCluster(mesos, kafkaClusterName.toLowerCase)


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
  def addCassandraNode(cpus: Double, memory: Double): Unit = {
    this.cassandraCluster.addNode(cpus, memory)
  }

  /**
    * Adds a kafka broker in the existing kafka cluster
    */
  def addKafkaBroker(cpus: Double, memory: Double): Unit = {
    this.kafkaCluster.addNode(cpus, memory)

  }

  /**
    * Start te spark framework on mesos to allow launching spark jobs in the cluster.
    *
    */
  def startSparkFramework(): Unit = {
    val sparkDispatcherTask = new TaskBuilder()
      .setId("spark-dispatcher")
      .setCpus(0.5).setMemory(512).setDisk(0)
      .setCmd(s"/root/${
        SmackEnvironment.SPARK_VERSION
      }/bin/spark-class " +
        "org.apache.spark.deploy.mesos.MesosClusterDispatcher " +
        s"--master mesos://${
          mesos.zkConnectionString
        }/mesos")
      .build()
    mesos.run(sparkDispatcherTask)


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
  def getCassandraInfo: Option[CassandraConnectionInfo] = {
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
  def getKafkaInfo: Option[KafkaConnectionInfo] = {
    this.kafkaCluster.getConnectionInfo
  }


}
