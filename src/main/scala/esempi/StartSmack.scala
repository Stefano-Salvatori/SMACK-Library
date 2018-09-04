package esempi

import cluster.MesosClusterBuilder
import smack.SmackEnvironmentBuilder

object StartSmack extends App {
  override def main(args: Array[String]) = {

    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("104.248.21.166"))
      .setAgents(List("104.248.19.183", "104.248.19.222", "142.93.163.237"))
      .setConnection("root", "private_key_openssh", "")
      .build()

    val smack = new SmackEnvironmentBuilder()
      .setMesosCluster(mesos)
      .setCassandraClusterName("CassandraCluster")
      .setCassandraNodesCount(0)
      .setKafkaBrokersCount(0)
      .build()

    //smack.startCassandraDb()
    //smack.startKafkaBrokers()
    smack.runSparkFramework()

  }


}
