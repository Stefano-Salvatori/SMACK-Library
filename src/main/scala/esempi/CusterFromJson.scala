package esempi

import cluster.MesosCluster
import task.GenericTask

object CusterFromJson extends App {
  override def main(args: Array[String]) = {


    val cluster = MesosCluster.fromJson("clusterConfig.json")
    cluster.createCluster()

    Thread.sleep(10000) //Wait that the cluster is full operative

    cluster.run(new GenericTask("TaskName", 1, 512, 0,
                                 Some("echo ciao;sleep 10 "),
                                 None, Map()))

  }

}
