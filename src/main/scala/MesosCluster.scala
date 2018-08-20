import java.io.{BufferedWriter, File, FileWriter}

import scala.sys.process._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Serialization.write
import sun.reflect.generics.reflectiveObjects.NotImplementedException

import scala.collection.mutable.ListBuffer

object MesosCluster {
  def apply(master: Node, agents: List[Node]): MesosCluster = {
    new MesosCluster(master, agents.to[ListBuffer])
  }

  def apply(master: Node): MesosCluster = {
    new MesosCluster(master, ListBuffer())
  }
}

case class MesosCluster(master: Node, agents: ListBuffer[Node])
  extends Cluster {
  private var requirementsInstalled = false
  private var clusterStarted = false
  private lazy val curlCmd = System.getProperty("os.name") match {
    case s if s.startsWith("Windows") => "curl.exe"
    case _ => "curl"
  }
  implicit val formats: DefaultFormats.type = DefaultFormats

  /**
    * Install the required software on each node on the cluster.
    */
  private def installComponents(): Unit = {
    master.executeScript(Scripts.INSTALL_MASTER)
    agents.foreach(a => a.executeScript(Scripts.INSTALL_AGENT))
    this.requirementsInstalled = true
  }

  /**
    * Start mesos masters , mesos agents and marathon framework to allow launching
    * applications over the cluster.
    *
    * @throws IllegalStateException if the installComponents() method was never called before
    */
  private def startCluster(): Unit = {
    if (!this.requirementsInstalled) throw new IllegalStateException()
    master.executeScript(Scripts.START_MASTER)
    agents.foreach(a => a.executeScript(Scripts.START_AGENT, master.getIp))
  }

  def startMarathon(): Unit = {
    master.executeCommand("screen -d -m marathon " +
      s"--master zk://${master.getIp}:2181/mesos " +
      s"--zk zk://${master.getIp}:2181/marathon " +
      "--task_launch_timeout 650000")
  }

  /**
    * Creates the mesos cluster; Automatically starts Marathon framework to allow launching
    * applications(commands, docker images...) over the cluster
    */
  def createCluster(): Unit = {
    this.installComponents()
    this.startCluster()
    this.startMarathon()
    this.clusterStarted = true
  }

  /**
    * Run an app on the cluster. Throws an IllegalStateException() if the cluster is not started
    *
    * @param marathonApp
    * the app to run
    */
  def run(marathonApp: MarathonApp): Unit = {
    if (!clusterStarted) throw new IllegalStateException()
    val bw = new BufferedWriter(new FileWriter(new File(s"${marathonApp.id}.json")))
    bw.write(write(marathonApp))
    (this.curlCmd + " -X POST -H \"Content-type: application/json\" " +
      s"$master:8080/v2/apps " +
      s"-d@${marathonApp.id}.json") !!
  }

  def getAgents: List[Node] = this.agents.toList

  def getMaster: Node = this.master

  /**
    * Add an agent node to the cluster
    *
    * @param node
    * the agent to add
    */
  override def addAgent(node: Node) = {
    println("Adding agent to the cluster")
    node.executeScript(Scripts.INSTALL_AGENT)
    node.executeScript(Scripts.START_AGENT, master.getIp)
    this.agents += node
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
        this.agents -= n
        println("Node removed")
    }

  }
}
