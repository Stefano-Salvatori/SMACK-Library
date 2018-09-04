package smack

import cluster.MesosCluster

class SmackEnvironmentBuilder {

  private var mesos: MesosCluster = _
  private var cassandraClusterName = ""
  private var cassandraNodesCount = 0
  private var kafkaBrokersCount = 0

  def setMesosCluster(cluster: MesosCluster): SmackEnvironmentBuilder = {
    this.mesos = cluster
    this
  }

  def setCassandraClusterName(name: String): SmackEnvironmentBuilder = {
    this.cassandraClusterName = name
    this
  }

  def setCassandraNodesCount(nodesCount: Int): SmackEnvironmentBuilder = {
    this.cassandraNodesCount = nodesCount
    this
  }

  def setKafkaBrokersCount(brokersCount: Int): SmackEnvironmentBuilder = {
    this.kafkaBrokersCount = brokersCount
    this
  }

  def build(): SmackEnvironment = {
    new SmackEnvironment(mesos, cassandraClusterName, cassandraNodesCount, kafkaBrokersCount)
  }


}
