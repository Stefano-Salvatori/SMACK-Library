package cluster

/**
  * A cluster represents a set of connected machines; some of them acts as masters and some as
  * agents. It's possible to add/replace agent nodes while master nodes must remain the same.
  *
  */
trait Cluster {

  val clusterName: String

  val masters: List[Node]

  var agents: List[Node]

  def createCluster()

  def shutdownCluster()

  def addAgent(node: Node)

  def removeAgent(node: Node)

}
