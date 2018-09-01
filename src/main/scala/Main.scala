import cluster.{ClusterConfigurations, MesosCluster, MesosClusterBuilder, Node}
import net.liftweb.json.parse
import task.CassandraTask.CassandraVariable
import task.KafkaTask.KafkaVariable
import task._

import scala.io.Source

object Main {
  val KAFKA_STARTING_OUTSIDE_PORT = 9095
  val KAFKA_STARTING_INSIDE_PORT = 9093


  def main(args: Array[String]): Unit = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val fileContents: String = Source.fromFile("config.json").getLines.mkString
    val clusterConfigurations: ClusterConfigurations = parse(fileContents).extract[ClusterConfigurations]

    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setAgents(List())
      .setMasters(List("11.11.11.11"))
      .setConnection("root", "private_key_openssh", "")
      .build()

    val mesos2 = MesosCluster(clusterConfigurations)

    println(mesos)


    //CASSANDRA
    /*for (c <- 1 to config.cassandraInstances) {
      val cassandra = new CassandraTask(s"cassandra$c", 2, 2048, 0, None)
      cassandra.set(CassandraVariable.CASSANDRA_CLUSTER_NAME, "cassandra-cluster")
      cassandra.set(CassandraVariable.CASSANDRA_SEEDS, mesos.getAgents.map(_.getIp).mkString(","))
      mesos.run(cassandra)
    }*/


    //KAFKA
    /*for (k <- 1 to config.kafkaInstances) {
      val insidePort = KAFKA_STARTING_INSIDE_PORT - k
      val outsidePort = KAFKA_STARTING_OUTSIDE_PORT - k
      val kafka = new KafkaTask(s"kafka$k", cpus = 1.5, mem = 1024, disk = 0, cmd = None)
      kafka.set(KafkaVariable.HOSTNAME_COMMAND,"ip route get 8.8.8.8 | awk '{print ; exit}'")
      kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT, s"${mesos.getMasters.map(_.getIp).mkString(",")}:2181/kafka")
      kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS, s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort")
      kafka.set(KafkaVariable.KAFKA_LISTENERS, s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort")
      kafka.set(KafkaVariable.KAFKA_LISTENER_SECURITY_PROTOCOL_MAP,"INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT")
      kafka.set(KafkaVariable.KAFKA_INTER_BROKER_LISTENER_NAME, "INSIDE")
      mesos.run(kafka)
    }*/

    //SPARK
    //start spark framework
    /*mesos.getMaster.executeCommand("./spark-2.3.1-bin-hadoop2.7/sbin/spark-daemon.sh " +
      "start org.apache.spark.deploy.mesos.MesosClusterDispatcher 1 " +
      s"--host ${mesos.getMaster.getIp} --port 7077 " +
      s"--conf spark.driver.host=${mesos.getMaster.getIp} " +
      s"--master mesos://${mesos.getZkConnectionString}")*/


  }
}
