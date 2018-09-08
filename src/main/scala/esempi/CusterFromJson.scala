package esempi

import cluster.{MesosCluster, Node}
import task.TaskBuilder

object CusterFromJson extends App {
  override def main(args: Array[String]) = {
    val cluster = MesosCluster.fromJson("clusterConfig.json")
    cluster.createCluster()

  }
}
