package esempi

import cluster.MesosClusterBuilder
import smack.SmackEnvironment

object StartSmack extends App {
  override def main(args: Array[String]) = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("159.65.127.232"))
      .setAgents(List("159.65.114.239", "159.65.113.201", "159.65.117.154"))
      .setConnection("root", "C:\\Users\\stefa\\Desktop\\private_key_openssh", "")
      .build()
    val smack = new SmackEnvironment(mesos, "cassandra-cluster", "kafka-cluster")
    smack.startCassandraCluster(serversCount = 2, cpus = 2, memory = 2048)

    smack.startKafkaCluster(brokersCount = 1, cpus = 2, memory = 2048)

    smack.startSparkFramework()

  }
}
