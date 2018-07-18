import java.io.{BufferedWriter, File, FileWriter}

import com.decodified.scalassh._
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import scala.sys.process._
import net.liftweb.json._
import net.liftweb.json.Serialization.write


import scala.io.Source


case class DockerContainer(image: String, network: String, forcePullImage: Boolean, privileged: Boolean, ports: Array[Int])


case class Container(docker: DockerContainer)


case class MarathonApp(var id: String, container: Container, env: Map[String, String], cpus: Double, mem: Double, instances: Int)

case class SmackConfiguration(user: String, password: String, masters: Array[String], agents: Array[String], cassandraInstances: Int, kafkaInstances: Int)

object SmackCluster {
  def apply(configurationFile: String) = new SmackCluster(configurationFile)
}

class SmackCluster(configurationFile: String) {

  implicit val formats: DefaultFormats.type = DefaultFormats
  lazy val fileContents: String = Source.fromFile(configurationFile).getLines.mkString

  lazy val json: JValue = parse(fileContents)
  val configurations: SmackConfiguration = json.extract[SmackConfiguration]
  lazy val master: String = configurations.masters(0)
  lazy val agents: Array[String] = configurations.agents
  lazy val usr: String = configurations.user
  lazy val psw: String = configurations.password
  lazy val connectionConfig = HostConfig(PasswordLogin(usr, SimplePasswordProducer(psw)), hostKeyVerifier = new PromiscuousVerifier())


  private def deployMarathonApp(marathonApp: MarathonApp) = {
    val bw = new BufferedWriter(new FileWriter(new File(s"${marathonApp.id}.json")))
    bw.write(write(marathonApp))
    bw.close()
    ("curl.exe -X POST -H \"Content-type: application/json\" " +
      s"$master:8080/v2/apps " +
      s"-d@${marathonApp.id}.json") !!
  }

  def installComponents(): Unit = {
    SSH(master, HostConfigProvider.fromHostConfig(connectionConfig)) {
      client => {
        client upload("scripts/setup_master.sh", ".")
        client exec "chmod u+x setup_master.sh"
        client exec "./setup_master.sh"
        client exec "screen -d -m marathon " +
          s"--master zk://$master:2181/mesos " +
          s"--zk zk://$master:2181/marathon " +
          "--task_launch_timeout 650000;"
      }
    }

    agents.foreach(SSH(_, HostConfigProvider.fromHostConfig(connectionConfig)) {
      client => {
        client upload("scripts/setup_agent.sh", ".")
        client exec "chmod u+x setup_agent.sh"
        client exec s"./setup_agent.sh $master"
      }
    })
  }

  def deployStack(): Unit = {
    //SPARK
    SSH(master, HostConfigProvider.fromHostConfig(connectionConfig)) {
      client =>
        client exec "./spark-2.3.1-bin-hadoop2.7/sbin/spark-daemon.sh " +
          "start org.apache.spark.deploy.mesos.MesosClusterDispatcher 1 " +
          s"--host $master --port 7077 " +
          s"--conf spark.driver.host=$master " +
          s"--master mesos://$master:5050"

    }
    //CASSANDRA
    val cassandra = parse(Source.fromFile("cassandra-template.json").getLines.mkString).extract[MarathonApp]
    val cassandraInstances = configurations.cassandraInstances
    cassandra.env("CASSANDRA_CLUSTERNAME") -> "cassandra-cluster"
    cassandra.env("CASSANDRA_SEEDS") -> s"${agents.mkString(",")}"
    for (c <- 1 to cassandraInstances) {
      cassandra.id = s"/cassandra-cluster/cassandra$c"
      deployMarathonApp(cassandra)
    }

    //KAFKA
    val kafkaApp =parse(Source.fromFile("kafka-template.json").getLines.mkString).extract[MarathonApp]
    val kafkaInstances = configurations.kafkaInstances

    kafkaApp.env("KAFKA_ZOOKEEPER_CONNECT") -> s"$master:2181/kafka"
    for (k <- 1 to kafkaInstances) {
      val insidePort = 9093 - k
      val outsidePort = 9095 - k
      kafkaApp.id = s"/kafka-cluster/kafka$k"
      kafkaApp.env("KAFKA_ADVERTISED_LISTENERS") -> s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      kafkaApp.env("KAFKA_LISTENERS") -> s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      deployMarathonApp(kafkaApp)
    }

  }
}
