package esempi

import cluster.MesosClusterBuilder
import smack.SmackEnvironmentBuilder

object StartSmack extends App {
  override def main(args: Array[String]) = {

    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("207.154.204.131"))
      .setAgents(List("139.59.144.165", "207.154.193.158","207.154.193.185"))
      .setConnection("root", "private_key_openssh", "")
      .build()

    val smack = new SmackEnvironmentBuilder()
      .setMesosCluster(mesos)
      .setCassandraClusterName("CassandraCluster")
      .setCassandraNodesCount(0)
      .setKafkaBrokersCount(3)
      .build()
    smack.startKafkaCluster()
    //smack.startCassandraDb()
    //Thread.sleep(15000)

    //smack.startKafkaBrokers()
    //smack.runSparkFramework()

  }


}
