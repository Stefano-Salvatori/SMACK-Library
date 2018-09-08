package esempi

import cluster.MesosCluster
import task.TaskBuilder

object RunTask extends App {
  override def main(args: Array[String]) = {
    val cluster = MesosCluster.fromJson("clusterConfig.json")
    cluster.createCluster()
    Thread.sleep(30000)
    val task = new TaskBuilder()
      .setId("task")
      .setMemory(1024)
      .setCpus(2)
      .setDisk(0)
      .setCmd("echo 'i'm a task' ; sleep 10")
      .build()
    cluster.run(task)
    Thread.sleep(10000)
    cluster.stop(task)

  }
}
