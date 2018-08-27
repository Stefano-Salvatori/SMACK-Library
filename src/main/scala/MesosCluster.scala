
import scala.sys.process._
import net.liftweb.json.DefaultFormats
import sun.reflect.generics.reflectiveObjects.NotImplementedException

object MesosCluster {
  private val DEFAULT_NAME: String = "Mesos Cluster"

  def apply(clusterName: String, masters: List[Node], agents: List[Node])
  : MesosCluster = {
    new MesosCluster(clusterName, masters, agents)
  }

  def apply(masters: List[Node], agents: List[Node]): MesosCluster = {
    new MesosCluster(DEFAULT_NAME, masters, agents)
  }

  def apply(masters: List[Node]): MesosCluster = {
    new MesosCluster(DEFAULT_NAME, masters, List())
  }


}

case class MesosCluster(clusterName: String, masters: List[Node], var agents: List[Node])
  extends Cluster {
  private lazy val curlCmd = System.getProperty("os.name") match {
    case s if s.startsWith("Windows") => "curl.exe"
    case _ => "curl"
  }
  private lazy val zkConnectionString = s"zk://${masters.map(_.getIp.concat(":2181")).mkString(",")}"
  implicit val formats: DefaultFormats.type = DefaultFormats

  /**
    * Install the required software on each node on the cluster.
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
    masters.foreach(node =>
      node.executeScript(Scripts.START_MULTIPLE_MASTER, clusterName :: masters.map(_.getIp): _*))
    agents.foreach(a => a.executeScript(Scripts.START_AGENT_MULTIPLE, masters.map(_.getIp): _*))
  }

  def startMarathon(): Unit = {
    masters.foreach(_.executeCommand("screen -d -m marathon " +
      s"--master $zkConnectionString/mesos " +
      s"--zk $zkConnectionString/marathon " +
      "--task_launch_timeout 650000"))
  }

  /**
    * Create and run the mesos cluster; Automatically starts Marathon framework to allow launching
    * applications(commands, docker images...) over the cluster
    */
  def createCluster(): Unit = {
    //this.installComponents()
    //this.startCluster()
    this.startMarathon()
  }

  /**
    * Run an app on the cluster. Throws an IllegalStateException() if the cluster is not started
    *
    * @param marathonTask
    * the app to run
    */
  def run(marathonTask: MarathonTask): Unit = {
    marathonTask.saveAsJson()
    (this.curlCmd + " -X POST -H \"Content-type: application/json\" " +
      s"$zkConnectionString/marathon:8080/v2/apps " +
      s"-d@${marathonTask.id}.json") !
  }

  def getAgents: List[Node] = this.agents

  def getMasters: List[Node] = this.masters

  /**
    * Add an agent node to the cluster
    *
    * @param node
    * the agent to add
    */
  override def addAgent(node: Node) = {
    println("Adding agent to the cluster")
    node.executeScript(Scripts.INSTALL_AGENT)
    node.executeScript(Scripts.START_MASTER, masters.map(_.getIp): _*)
    node :: this.agents
  }

  override def shutdownCluster() = {
    throw new NotImplementedException()
  }

  /**
    * Remove a node from the cluster
    *
    * @param node
    * the node to remove
    */
  def removeAgent(node: Node) = {
    this.removeAgent(node.getIp)
  }

  /**
    * Remove a node from the cluster
    *
    * @param ip
    * the node of the ip to remove
    */
  def removeAgent(ip: String): Unit = {
    this.agents.find(node => node.getIp.equals(ip)) match {
      case None => println("Cannot find a node with this ip")
      case Some(n) =>
        n.executeCommand("sudo service mesos-slave stop")
        this.agents = this.agents.filter(agent => agent != n)
        println("Node removed")
    }

  }
}
