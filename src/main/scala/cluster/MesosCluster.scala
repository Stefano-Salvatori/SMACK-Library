package cluster


import net.liftweb.json.DefaultFormats
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import task.MarathonTask

import scala.sys.process._

object MesosCluster {
  private val DEFAULT_NAME: String = "Mesos cluster.Cluster"

  def apply(clusterName: String, masters: List[Machine], agents: List[Machine])
  : MesosCluster = {
    new MesosCluster(clusterName, masters, agents)
  }

  def apply(masters: List[Machine], agents: List[Machine]): MesosCluster = {
    new MesosCluster(DEFAULT_NAME, masters, agents)
  }

  def apply(masters: List[Machine]): MesosCluster = {
    new MesosCluster(DEFAULT_NAME, masters, List())
  }


}

case class MesosCluster(clusterName: String, masters: List[Machine], var agents: List[Machine])
  extends Cluster {
  implicit val formats: DefaultFormats.type = DefaultFormats

  private lazy val curlCmd = System.getProperty("os.name") match {
    case s if s.startsWith("Windows") => "curl.exe"
    case _ => "curl"
  }
  private lazy val zkConnectionString = s"zk://${masters.map(_.getIp.concat(":2181")).mkString(",")}"

  /**
    * Install the required software on each machine of the cluster.
    */
  private def installComponents(): Unit = {
    masters.foreach(_.executeScript(Scripts.INSTALL_MASTER))
    agents.foreach(_.executeScript(Scripts.INSTALL_AGENT))
  }

  /**
    * Start mesos masters , mesos agents and marathon framework to allow launching
    * applications over the cluster.
    **/
  private def startCluster(): Unit = {
    masters.foreach(m =>
      m.executeScript(Scripts.START_MULTIPLE_MASTER, clusterName :: masters.map(_.getIp): _*))
    agents.foreach(a => a.executeScript(Scripts.START_AGENT_MULTIPLE, masters.map(_.getIp): _*))
  }

  def startMarathon(): Unit = {
    masters.foreach(m => m.executeScript(Scripts.START_MARATHON, masters.map(_.getIp): _*))
  }

  /**
    * Create and run the mesos cluster; Automatically starts Marathon framework to allow launching
    * applications(commands, docker images...) over the cluster
    */
  def createCluster(): Unit = {
    this.installComponents()
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

    //val bw = new BufferedWriter(new FileWriter(new File(s"response.json")))
    //println(prettyRender(parse(response)))
  }

  def getAgents: List[Machine] = this.agents

  def getMasters: List[Machine] = this.masters

  /**
    * Add an agent to the cluster
    *
    * @param machine
    * the agent to add
    */
  override def addAgent(machine: Machine) = {
    println("Adding agent to the cluster")
    machine.executeScript(Scripts.INSTALL_AGENT)
    machine.executeScript(Scripts.START_AGENT_MULTIPLE, masters.map(_.getIp): _*)
    machine :: this.agents
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
  def removeAgent(agent: Machine) = {
    if(this.agents.contains(agent))
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
}
