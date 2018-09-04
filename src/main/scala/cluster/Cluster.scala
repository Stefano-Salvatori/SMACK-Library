package cluster

trait Cluster {

  def getClusterName: String

  def createCluster()

  def shutdownCluster()

  def addAgent(node: Node)

  def removeAgent(node: Node)

  def getMasters: List[Node]

  def getAgents: List[Node]

}
