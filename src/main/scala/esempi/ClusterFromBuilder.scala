package esempi

import ch.ethz.ssh2.log.Logger
import cluster.MesosClusterBuilder
import task.GenericTask


object ClusterFromBuilder extends App {

  override def main(args: Array[String]): Unit = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("104.248.21.166"))
      .setAgents(List("104.248.19.183", "104.248.19.222", "142.93.163.237"))
      .setConnection("root", "private_key_openssh", "")
      .build()

    mesos.createCluster()
    Thread.sleep(20000) //Wait that the cluster is fully operative


  }

}
