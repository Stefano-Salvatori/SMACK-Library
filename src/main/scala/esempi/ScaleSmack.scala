package esempi

import cluster.{MesosCluster, MesosClusterBuilder}
import smack.SmackEnvironment


object ScaleSmack extends App {
  override def main(args: Array[String]) = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("46.101.231.133"))
      .setAgents(List("138.197.187.246", "165.227.160.90"))
      .setConnection("root", "C:\\Users\\stefa\\Desktop\\private_key_openssh", "")
      .build()
    val smack = new SmackEnvironment(mesos,"CassandraCluster","KafkaCluster")
    smack.addCassandraNode(1, 2048)
  }
}