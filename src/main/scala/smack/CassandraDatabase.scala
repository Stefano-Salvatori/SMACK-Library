package smack

class CassandraDatabase(private val cassandraClusterName: String,
                        private var nodesCount: Int) {

  def getName = this.cassandraClusterName

  def getNodesCount = this.nodesCount

  def addNode(): Unit = this.nodesCount += 1


}
