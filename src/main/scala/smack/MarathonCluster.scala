package smack

import cluster.MesosCluster
import net.liftweb.json.JsonAST.JField
import net.liftweb.json.parse
import task.Task
import utils.Utils

import scala.sys.process._


/**
  * Represents an abstraction of a cluster running as a marathon service inside mesos
  */
private trait MarathonCluster {
  implicit val formats = net.liftweb.json.DefaultFormats
  val mesos: MesosCluster
  val clusterName: String
  protected var runningNodes = mesos.getTaskInfo(this.clusterName) match {
    case Some(info) =>
      (parse(info) findField {
        case JField(n, _) => n == "instances"
      }).get.value.extract[Int]
    case None => 0
  }

  /**
    *
    * @param cpus
    *             the cpu to assing to this task
    * @param memory
    *               the memory reserved for this task
    * @return
    */
  protected def nodeTask(cpus: Double, memory: Double): Task

  /**
    * Starts the cluster on mesos.
    *
    * @param nodes
    * number of nodes in the cluster
    * @param cpus
    * cpu reserved for each node
    * @param memory
    * memory for each node
    */
  def start(nodes: Int, cpus: Double, memory: Double) = {
    if (getRunningNodes != 0) {
      throw new IllegalStateException("This cluster is already started")
    } else {
      val task = this.nodeTask(cpus,memory)
      task.instances = nodes
      mesos.run(task)
      this.runningNodes += nodes
    }
  }

  def getConnectionInfo: Option[_]

  /**
    * Adds a node in the existing cluster
    */
  def addNodes(howMany: Int): Unit = {
    this.runningNodes += howMany
    val msg =s""" "{\"\"\"instances\"\"\":${this.runningNodes}}" """
    val response = Utils.curlCmd + " -X PATCH -H \"Content-type: application/json\" " +
      s"${mesos.masters.head.getIp}:8080/v2/apps/${this.clusterName} -d $msg " !!

    //println(prettyRender(parse(response)))
  }

  def getRunningNodes: Int = this.runningNodes

}
