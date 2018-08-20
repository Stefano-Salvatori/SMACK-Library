trait Cluster {

  def createCluster()

  def shutdownCluster()

  def addAgent(node: Node)

  def removeAgent(node: Node)

  def getMaster: Node

  def getAgents: List[Node]




}
