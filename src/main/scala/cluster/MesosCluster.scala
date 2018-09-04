package cluster

import java.io.{BufferedWriter, File, FileWriter}

import net.liftweb.json.{DefaultFormats, parse}
import net.liftweb.json.prettyRender
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import task.MarathonTask

import scala.io.Source
import scala.sys.process._

object MesosCluster {
  private val DEFAULT_NAME: String = "Mesos-Cluster"

  def fromJson(jsonPath: String): MesosCluster = {
    implicit val formats = net.liftweb.json.DefaultFormats
    val fileContents: String = Source.fromFile(jsonPath).getLines.mkString
    val config: ClusterConfigurations = parse(fileContents).extract[ClusterConfigurations]
    new MesosClusterBuilder()
      .setClusterName(config.clusterName)
      .setMasters(config.masters)
      .setAgents(config.agents)
      .setConnection(config.user, config.sshKeyPath, config.sshKeyPassword)
      .build()
  }

}

class MesosCluster(val clusterName: String,
                   val masters: List[Node],
                   var agents: List[Node])
  extends Cluster {


  implicit val formats: DefaultFormats.type = DefaultFormats
  private lazy val curlCmd = System.getProperty("os.name") match {
    case s if s.startsWith("Windows") => "curl.exe"
    case _ => "curl"
  }
  private lazy val zkConnectionString = s"zk://${masters.map(_.getIp.concat(":2181")).mkString(",")}"

  def getZkConnectionString: String = this.zkConnectionString

  override def getClusterName = this.clusterName

  def getAgents: List[Node] = this.agents

  def getMasters: List[Node] = this.masters

  /**
    * Create and run the mesos cluster; install all the required software in mastes
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
  def run(marathonTask: MarathonTask): Unit = {
    marathonTask.saveAsJson()
    val response = (this.curlCmd + " -X POST -H \"Content-type: application/json\" " +
      s"${masters.head.getIp}:8080/v2/apps " +
      s"-d@${marathonTask.id}.json") !!

    new File(s"${marathonTask.id}.json").delete()
    println(prettyRender(parse(response)))
  }

  /**
    * Add an agent to the cluster
    *
    * @param node
    * the agent to add
    */
  override def addAgent(node: Node) = {
    println("Adding agent to the cluster")
    node.executeScript(Scripts.INSTALL_AGENT, printResult = true)
    this.agents = node :: this.agents
    this.setHostnames()
    node.executeScript(Scripts.START_AGENT_MULTIPLE, printResult = true, masters.map(_.getIp): _*)
  }

  override def shutdownCluster() = {
    throw new NotImplementedException()
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
        n.executeCommand("sudo service mesos-slave stop")
        this.agents = this.agents.filter(agent => agent != n)
    }

  }

  def setHostnames(): Unit = {
    val all = masters ::: agents
    all.foreach(node => {
      val args = all.filter(_ != node).map(n => s"${n.getIp} ${n.getHostname}").mkString(" ")
      node.executeScript(Scripts.SET_HOSTS, printResult = true, args)
    })
  }

  /**
    * Install the required software on each node of the cluster.
    */
  private def installComponents(): Unit = {
    masters.foreach(_.executeScript(Scripts.INSTALL_MASTER, printResult = true))
    agents.foreach(_.executeScript(Scripts.INSTALL_AGENT, printResult = true))
  }

  /**
    * Start mesos masters and mesos agents
    **/
  def startCluster(): Unit = {
    masters.foreach(m => m.executeScript(Scripts.START_MULTIPLE_MASTER, printResult = true,
                                          clusterName :: masters.map(_.getIp): _*))
    agents.foreach(a => a.executeScript(Scripts.START_AGENT_MULTIPLE, printResult = true,
                                         masters.map(_.getIp): _*))
  }

  /**
    * Start marathon framework to allow launching
    * applications over the cluster.
    */
  private def startMarathon(): Unit = {
    masters.foreach(m =>
      m.executeScript(Scripts.START_MARATHON, printResult = true, masters.map(_.getIp): _*))
  }

}
