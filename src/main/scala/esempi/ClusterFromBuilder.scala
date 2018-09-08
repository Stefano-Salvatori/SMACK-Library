package esempi
import cluster.{MesosClusterBuilder, Node}

object ClusterFromBuilder extends App {
  override def main(args: Array[String]): Unit = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("207.154.204.131"))
      .setAgents(List("139.59.144.165", "207.154.193.158", "207.154.193.185"))
      .setConnection("root", "private_key_openssh", "")
      .build()
    mesos.createCluster()
    Thread.sleep(30000)
    mesos.addAgent(new Node("207.154.54.34", "root", "private_key_openssh",""))
  }
}
