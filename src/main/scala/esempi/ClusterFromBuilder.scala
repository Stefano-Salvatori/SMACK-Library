package esempi
import cluster.{MesosClusterBuilder, Node}

object ClusterFromBuilder extends App {
  override def main(args: Array[String]): Unit = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("159.65.127.232"))
      .setAgents(List("159.65.114.239", "159.65.113.201","159.65.117.154"))
      .setConnection("root", "C:\\Users\\stefa\\Desktop\\private_key_openssh", "")
      .build()
    mesos.createCluster()
    //mesos.addAgent(new Node("207.154.54.34", "root", "private_key_openssh",""))
  }
}
