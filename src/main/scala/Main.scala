import java.io.{BufferedWriter, File, FileWriter}

import net.liftweb.json.Serialization.write
import net.liftweb.json.{DefaultFormats, parse}

import scala.io.Source

object Main {
  val KAFKA_STARTING_OUTSIDE_PORT = 9095
  val KAFKA_STARTING_INSIDE_PORT = 9093

  case class SmackConfiguration(user: String,
                                password: String,
                                masters: List[String],
                                agents: List[String],
                                cassandraInstances: Int,
                                kafkaInstances: Int)

  def main(args: Array[String]): Unit = {
    implicit val formats: DefaultFormats.type = DefaultFormats
    val fileContents: String = Source.fromFile("config.json").getLines.mkString
    val configurations: SmackConfiguration = parse(fileContents).extract[SmackConfiguration]

    val mesos = MesosCluster(
                              configurations.masters
                                .map(s => Node(s, configurations.user, configurations.password))
                                .head,
                              configurations.agents
                                .map(s => Node(s, configurations.user, configurations.password))
                            )



    //mesos.createCluster()
    //SPARK
    //start spark framework
    /*mesos.getMaster.executeCommand("./spark-2.3.1-bin-hadoop2.7/sbin/spark-daemon.sh " +
      "start org.apache.spark.deploy.mesos.MesosClusterDispatcher 1 " +
      s"--host ${mesos.getMaster.getIp} --port 7077 " +
      s"--conf spark.driver.host=${mesos.getMaster.getIp} " +
      s"--master mesos://${mesos.getMaster.getIp}:5050")*/

    //CASSANDRA
    val cassandra = MarathonTask("cassandra-app.json")
    cassandra.saveAsJson("a")
    cassandra.env += "CASSANDRA_CLUSTERNAME" -> "cassandra-cluster"
    cassandra.env += "CASSANDRA_SEEDS" -> mesos.getAgents.map(_.getIp).mkString(",")
    for (c <- 1 to configurations.cassandraInstances) {
      cassandra.id = s"cassandra$c"
      mesos.run(cassandra)
    }

    //KAFKA
    val kafka = MarathonTask("kafka-app.json")
    kafka.env += "KAFKA_ZOOKEEPER_CONNECT" -> s"${mesos.getMaster.getIp}:2181/kafka"
    for (k <- 1 to configurations.kafkaInstances) {
      val insidePort = KAFKA_STARTING_INSIDE_PORT - k
      val outsidePort = KAFKA_STARTING_OUTSIDE_PORT - k
      kafka.id = s"kafka$k"
      kafka.env += "KAFKA_ADVERTISED_LISTENERS" -> (s"INSIDE://:$insidePort," +
        s"OUTSIDE://:$outsidePort")
      kafka.env += "KAFKA_LISTENERS" -> s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      mesos.run(kafka)
    }


  }
}
