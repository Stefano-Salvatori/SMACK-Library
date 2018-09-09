package esempi
import cluster.{MesosClusterBuilder, Node}

object ClusterFromBuilder extends App {
  override def main(args: Array[String]): Unit = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("138.68.96.19"))
      .setAgents(List("46.101.185.12", "46.101.208.135"))
      .setConnection("root", "private_key_openssh", "")
      .build()
    mesos.createCluster()
    Thread.sleep(30000)
    //mesos.addAgent(new Node("207.154.54.34", "root", "private_key_openssh",""))
  }
}
