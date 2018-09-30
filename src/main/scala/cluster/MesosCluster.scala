package cluster

import java.io.{File, FileNotFoundException}

import cluster.MesosCluster._
import net.liftweb.json.{DefaultFormats, parse}
import task.Task
import utils.Utils

import scala.io.Source
import scala.sys.process._

object MesosCluster {

  case class JsonConfig(clusterName: String,
                        user: String,
                        sshKeyPath: String,
                        sshKeyPassword: String,
                        masters: List[String],
                        agents: List[String])

  private val DEFAULT_NAME: String = "Mesos-Cluster"

  def fromJson(jsonPath: String): MesosCluster = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val fileContents: String = Source.fromFile(jsonPath).getLines.mkString
    val config: JsonConfig = parse(fileContents).extract[JsonConfig]
    new MesosClusterBuilder()
      .setClusterName(config.clusterName)
      .setMasters(config.masters)
      .setAgents(config.agents)
      .setConnection(config.user, config.sshKeyPath, config.sshKeyPassword)
      .build()
  }

  private object InstallMaster extends Script("scripts/install_master.sh")

  private object InstallAgent extends Script("scripts/install_agent.sh")

  private object StartMaster extends Script("scripts/scala_start_master.scala")

  private object StartAgent extends Script("scripts/scala_start_agent.scala")

  private object StartMarathon extends Script("scripts/scala_start_marathon.scala")

  private object SetHosts extends Script("scripts/scala_set_hosts.scala")

  private object StopMaster extends Script("scripts/scala_stop_master.scala")

  private object StopAgent extends Script("scripts/scala_stop_agent.scala")

}

/**
  *
  * @param clusterName
  * 'Symbolic' name of the cluster
  * @param masters
  * List of nodes that will become masters
  * @param agents
  * List of nodes that wil become agents
  */
class MesosCluster(val clusterName: String, val masters: List[Node], var agents: List[Node])
  extends Cluster {
  if (clusterName.contains(" ")) throw new IllegalArgumentException("Invalid cluster name")
  private implicit val formats: DefaultFormats.type = DefaultFormats

  /**
    * @return
    * the string to connect to zookeeper
    */
  def zkConnectionString: String = s"zk://${masters.map(_.getIp.concat(":2181")).mkString(",")}"

  /**
    * Create and run the mesos cluster; install all the required software in masters
    * and agents nodes; automatically starts Marathon framework to allow launching
    * applications(commands, docker images...) over the cluster
    */
  def createCluster(): Unit = {
    this.installComponents()
    this.setHostnames()
    this.startCluster()
    this.startMarathon()
  }

  /**
    * Run an app on the cluster.
    *
    * @param marathonTask
    * the app to run
    */
  def run(marathonTask: Task): Unit = {
    marathonTask.saveAsJson()
    Utils.curlCmd + " -X POST -H \"Content-type: application/json\" " +
      s"${masters.head.getIp}:8080/v2/apps " +
      s"-d@${marathonTask.id}.json" !!

    new File(s"${marathonTask.id}.json").delete()
    //println(prettyRender(parse(response)))
  }

  /**
    * Kill a task running in the cluster
    *
    * @param marathonTask
    * the task to kill
    */
  def stop(marathonTask: Task): Unit = {
    s"${Utils.curlCmd} -X DELETE ${masters.head.getIp}:8080/v2/apps/${marathonTask.id}" !!
  }

  /**
    * see [[cluster.MesosCluster#getTaskInfo]]
    */
  def getTaskInfo(id: String): Option[String] = {
    try {
      Some(scala.io.Source.fromURL(s"http://${masters.head.getIp}:8080/v2/apps/$id").mkString)
    } catch {
      case noApp: FileNotFoundException => None
    }
  }

  /**
    * Get the information of the task in input as a json string.
    *
    * @param marathonTask
    * the task to look for
    * @return
    * an optional that contains the json string or None if the task doesn't exist
    */
  def getTaskInfo(marathonTask: Task): Option[String] = {
    try {
      Some(scala.io.Source.fromURL(s"http://${masters.head.getIp}:8080/v2/apps/${marathonTask.id}")
        .mkString)
    } catch {
      case _: FileNotFoundException => None
    }

  }

  /**
    * Add an agent to the cluster
    *
    * @param node
    * the agent to add
    */
  override def addAgent(node: Node) = {
    println("Adding agent to the cluster")
    node.executeScript(InstallAgent)
    this.agents = node :: this.agents
    this.setHostnames()
    node.executeScript(StartAgent, masters.map(_.getIp): _*)
  }

  /**
    * Shutdown the cluster. (All running tasks will be killed)
    */
  override def shutdownCluster() = {
    masters.foreach(_.executeScript(StopMaster))
    agents.foreach(_.executeScript(StopAgent))
  }

  /**
    * Remove an agent from the cluster
    *
    * @param agent
    * the agent to remove
    */
  def removeAgent(agent: Node) = {
    if (this.agents.contains(agent))
      this.removeAgent(agent.getIp)
    else println("Cannot find the agent in input")
  }

  /**
    * Remove an agent from the cluster
    *
    * @param ip
    * the ip of the agent to remove
    */
  def removeAgent(ip: String): Unit = {
    this.agents.find(agent => agent.getIp.equals(ip)) match {
      case None => println("Cannot find an agent with this ip")
      case Some(n) =>
        n.executeScript(StopAgent)
        this.agents = this.agents.filter(agent => agent != n)
    }

  }

  private def setHostnames(): Unit = {
    val all = masters ::: agents
    all.foreach(node => {
      val args = all.filter(_ != node).map(n => s"${n.getIp} ${n.getHostname}").mkString(" ")
      node.executeScript(SetHosts, args)
    })
  }

  /**
    * Install the required software on each node of the cluster.
    */
  private def installComponents(): Unit = {
    masters.foreach(_.executeScript(InstallMaster))
    agents.foreach(_.executeScript(InstallAgent))
  }

  /**
    * Start mesos masters and mesos agents
    **/
  private def startCluster(): Unit = {
    masters.foreach(m => m.executeScript(StartMaster, clusterName :: masters.map(_.getIp): _*))
    agents.foreach(a => a.executeScript(StartAgent, masters.map(_.getIp): _*))
  }

  /**
    * Start marathon framework to allow launching
    * applications over the cluster.
    */
  private def startMarathon(): Unit = {
    masters.foreach(m => m.executeScript(StartMarathon, masters.map(_.getIp): _*))
  }

}
