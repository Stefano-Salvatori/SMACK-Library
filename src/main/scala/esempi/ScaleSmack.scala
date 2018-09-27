package esempi

import cluster.MesosCluster
import smack.SmackEnvironment

object ScaleSmack extends App {
  override def main(args: Array[String]) = {
    val mesos = MesosCluster.fromJson("clusterConfig.json")
    mesos.createCluster()
    val smack = new SmackEnvironment(mesos, "cassandra-cluster", "kafka-cluster")
    smack.startCassandraCluster(serversCount = 2, cpus = 2, memory = 2048)
    smack.startKafkaCluster(brokersCount = 1, cpus = 2, memory = 2048)
    smack.startSparkFramework()

    Thread.sleep(1000)

    smack.addCassandraNodes(1)
    smack.addKafkaBrokers(2)
  }
}