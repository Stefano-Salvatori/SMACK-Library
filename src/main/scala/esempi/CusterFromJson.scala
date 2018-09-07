package esempi

import cluster.MesosCluster
import task.{GenericTask, TaskBuilder}

object CusterFromJson extends App {
  override def main(args: Array[String]) = {


    val cluster = MesosCluster.fromJson("clusterConfig.json")
    cluster.createCluster()

    Thread.sleep(10000) //Wait that the cluster is full operative

    cluster.run(new TaskBuilder()
      .id("task")
      .cpus(1)
      .mem(512)
      .cmd("echo smack;sleep 10")
      .build())

  }

}
