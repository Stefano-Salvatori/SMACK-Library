package smack

import java.io.{File, FileNotFoundException}

import cluster.MesosCluster
import com.datastax.driver.core.{Cluster, Session}
import net.liftweb.json.JsonAST.{JField, JInt, JValue}
import task.CassandraTask.CassandraVariable
import task.KafkaTask.KafkaVariable
import task.{CassandraTask, KafkaTask, TaskBuilder}
import utils.Utils
import net.liftweb.json.{JsonAST, parse, prettyRender}

import scala.language.postfixOps
import scala.sys.process._

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

  implicit val formats = net.liftweb.json.DefaultFormats

  cassandraClusterName = cassandraClusterName.toLowerCase()
  kafkaClusterName = kafkaClusterName.toLowerCase()

  private var cassandraNodesCount = mesos.getTaskInfo(this.cassandraClusterName) match {
    case Some(info) =>
      (parse(info) findField {
        case JField(n, v) => n == "instances"
      }).get.value.extract[Int]
    case None => 0
  }


  private var kafkaBrokersCount = mesos.getTaskInfo(this.kafkaClusterName) match {
    case Some(info) =>
      (parse(info) findField {
        case JField(n, v) => n == "instances"
      }).get.value.extract[Int]
    case None => 0
  }

  /**
    * Start a cassandra database with the configurations (name,nodes count..) specified
    * in this smack environment object
    */
  def startCassandraDatabase(serversCount: Int, cpus: Double, memory: Double)
  : Unit = {
    if (this.cassandraNodesCount != 0) {
      this.cassandraNodesCount += serversCount
      val task = new CassandraTask(this.cassandraClusterName, cpus, memory, 0, None, serversCount)
      task.set(CassandraVariable.CASSANDRA_CLUSTER_NAME, this.cassandraClusterName)
      task.set(CassandraVariable.CASSANDRA_SEEDS, mesos.agents.map(_.getIp).mkString(","))
      mesos.run(task)
    } else {
      throw new IllegalStateException("Cassandra cluster already exists. " +
        "Use 'addCassandraNode' to scale it up")
    }
  }

  /**
    * Start a kafka cluster with the configurations (brokers count...) specified
    * in this smack environment object
    */
  def startKafkaCluster(brokersCount: Int, cpus: Double, memory: Double): Unit = {
    if (this.kafkaBrokersCount != 0) {
      this.kafkaBrokersCount += brokersCount
      val kafka = new KafkaTask(this.kafkaClusterName, cpus, memory, disk = 0,
                                 cmd = None, instances = brokersCount)
      kafka.set(KafkaVariable.HOSTNAME_COMMAND,
                 "ip -4 route get 8.8.8.8 | awk {'print $7'} | tr -d '\\n'")
      kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT,
                 s"${mesos.masters.map(_.getIp).mkString(",")}:2181/kafka")
      kafka.set(KafkaVariable.KAFKA_LISTENERS,
                 "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
      kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS,
                 "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
      kafka.set(KafkaVariable.KAFKA_LISTENER_SECURITY_PROTOCOL_MAP,
                 "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT")
      kafka.set(KafkaVariable.KAFKA_INTER_BROKER_LISTENER_NAME, "INSIDE")
      kafka.setEnvVariable("KAFKA_DELETE_TOPIC_ENABLE", "true")

      mesos.run(kafka)
    }
    else {
      throw new IllegalStateException("Kafka cluster already exists. " +
        "Use 'addKafkaBroker' to scale it up")
    }
  }

  /**
    * Adds a node in the existing cassandra cluster
    */
  def addCassandraNode(cpus: Double, memory: Double): Unit = {
    this.cassandraNodesCount += 1
    val triplet = "\"\"\""
    val msg =
      s""" "{${
        triplet
      }instances${
        triplet
      }:${
        this.cassandraNodesCount
      }}" """
    val response = Utils.curlCmd + " -X PATCH -H \"Content-type: application/json\" " +
      s"${
        mesos.masters.head.getIp
      }:8080/v2/apps/${
        this.cassandraClusterName
      } -d $msg " !!

    //println(prettyRender(parse(response)))
  }

  /**
    * Adds a kafka broker in the existing kafka cluster
    */
  def addKafkaBroker(cpus: Double, memory: Double): Unit = {
    this.kafkaBrokersCount += 1
    Utils.curlCmd + " -X PATCH -H \"Content-type: application/json\" " +
      s"${
        mesos.masters.head.getIp
      }:8080/v2/apps/${
        this.kafkaClusterName
      } " +
      s"""-d '{"instances":${
        this.kafkaBrokersCount
      }""" !!

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


  def getCassandraSession: Session = {
    val builder = new Cluster.Builder
    mesos.agents.foreach(a => builder.addContactPoint(a.getIp))
    val cluster = builder
      .withPort(9042)
      .build()
    cluster.connect()
  }

}
