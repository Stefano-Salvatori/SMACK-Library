package esempi
import cluster.MesosClusterBuilder

object ClusterFromBuilder extends App {
  override def main(args: Array[String]): Unit = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("159.65.127.232"))
      .setAgents(List("159.65.114.239", "159.65.113.201","159.65.117.154"))
      .setConnection("root", "private_key_openssh", "")
      .build()
    mesos.createCluster()
  }
}
