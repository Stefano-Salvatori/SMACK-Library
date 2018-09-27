package esempi

import cluster.{MesosCluster, Node}

object ScaleCluster extends App {

  override def main(args: Array[String]) = {
    val cluster = MesosCluster.fromJson("clusterConfig.json")
    cluster.createCluster()
    Thread.sleep(1000)
    val node1: Node = new Node("178.43.54.212","root","sshKeyPath","")
    val node2: Node =new Node("178.43.54.213","root","sshKeyPath","")
    cluster.addAgent(node1)
    cluster.addAgent(node2)
    Thread.sleep(1000)
    cluster.removeAgent(node2)

  }

}
