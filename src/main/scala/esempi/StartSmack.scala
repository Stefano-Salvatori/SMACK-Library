package esempi

import cluster.MesosClusterBuilder
import smack.SmackEnvironment

object StartSmack extends App {
  override def main(args: Array[String]) = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("207.154.204.131"))
      .setAgents(List("139.59.144.165", "207.154.193.158", "207.154.193.185"))
      .setConnection("root", "private_key_openssh", "")
      .build()
    mesos.createCluster()
    Thread.sleep(30000)

    val smack = new SmackEnvironment(mesos)
    smack.startCassandraDatabase("CassandraCluster", 3, 2, 2048)
    smack.startKafkaCluster(3, 2, 1024)
    smack.startSparkFramework()
  }
}
