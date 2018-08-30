package cluster

trait Cluster {

  val clusterName: String

  def createCluster()

  def shutdownCluster()

  def addAgent(node: Machine)

  def removeAgent(node: Machine)

  def getMasters: List[Machine]

  def getAgents: List[Machine]

}
