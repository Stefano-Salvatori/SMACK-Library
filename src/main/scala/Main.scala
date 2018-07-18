import java.io.{BufferedWriter, File, FileWriter}

import scala.sys.process._
import com.decodified.scalassh._
import net.liftweb.json.{DefaultFormats, JValue, parse}
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import ujson.Js
import net.liftweb.json.Serialization.write


import scala.io.Source


object Main {

  def main(args: Array[String]): Unit = {
    val filename = "config-cluster.json"
    val fileContents = Source.fromFile(filename).getLines.mkString

    implicit val formats: DefaultFormats.type = DefaultFormats
    val json: JValue = parse(fileContents)
    val configurations: SmackConfiguration = json.extract[SmackConfiguration]
    val master: String = configurations.masters(0)
    val agents: Array[String] = configurations.agents
    val usr: String = configurations.user
    val psw: String = configurations.password
    val connectionConfig = HostConfig(PasswordLogin(usr, SimplePasswordProducer(psw)), hostKeyVerifier = new PromiscuousVerifier())
    val cassandra = parse(Source.fromFile("cassandra-template.json").getLines.mkString).extract[MarathonApp]
    val bw = new BufferedWriter(new FileWriter(new File(s"cassandraTest.json")))
    bw.write(write(cassandra))
    bw.close()
    //var config = HostConfig(PasswordLogin(usr, SimplePasswordProducer(psw)), hostKeyVerifier = new PromiscuousVerifier())

    val cluster = SmackCluster("config-cluster.json")
    cluster.installComponents()
    cluster.deployStack()

    //INSTALL COMPONENTS
    /*SSH(master, HostConfigProvider.fromHostConfig(config)) {
      client => {
        client upload ("scripts/setup_master.sh", ".")
        client exec "chmod u+x setup_master.sh"
        client exec "./setup_master.sh"
        client exec "screen -d -m marathon " +
          s"--master zk://$master:2181/mesos " +
          s"--zk zk://$master:2181/marathon " +
          "--task_launch_timeout 650000;"
      }
    }*/


    /*agents.arr.map(_.str)
      .foreach(SSH(_, HostConfigProvider.fromHostConfig(config)) {
        client => {
          client upload ("scripts/setup_agent.sh", ".")
          client exec "chmod u+x setup_agent.sh"
          client exec s"./setup_agent.sh $master"
        }
      })*/


    //SPARK
    /*SSH(master, HostConfigProvider.fromHostConfig(config)) {
      client => client exec "./spark-2.3.1-bin-hadoop2.7/sbin/spark-daemon.sh " +
         "start org.apache.spark.deploy.mesos.MesosClusterDispatcher 1 " +
        s"--host $master --port 7077 " +
        s"--conf spark.driver.host=$master " +
        s"--master mesos://$master:5050"

    }*/


    //CASSANDRA
    /*val cassandraTemplate = ujson.read(Source.fromFile("cassandra-template.json").getLines
      .mkString)
    val cassandraInstances = json("cassandra-instances").num.intValue

    cassandraTemplate("env")("CASSANDRA_SEEDS") = s"${agents.arr.map(_.str).mkString(",")}"
    for (c <- 1 to cassandraInstances) {
      cassandraTemplate("id") = Js.JsonableString(s"/cassandra-cluster/cassandra$c")
      val bw = new BufferedWriter(new FileWriter(new File(s"cassandra$c.json")))
      bw.write(cassandraTemplate.toString())
      bw.close()
      ("curl.exe -X POST -H \"Content-type: application/json\" " +
          s"$master:8080/v2/apps " +
          s"-d@cassandra$c.json")!!
    }

    //KAFKA
    val kafkaTemplate = ujson.read(Source.fromFile("kafka-template.json").getLines.mkString)
    val kafkaInstances = json("kafka-instances").num.intValue

    val env = kafkaTemplate("env")
    env("KAFKA_ZOOKEEPER_CONNECT") = s"$master:2181/kafka"
    for (k <- 1 to kafkaInstances) {
      val insidePort = 9093 - k
      val outsidePort = 9095 - k
      kafkaTemplate("id") = Js.JsonableString(s"/kafka-cluster/kafka$k")
      env("KAFKA_ADVERTISED_LISTENERS") = s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      env("KAFKA_LISTENERS") = s"INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      val bw = new BufferedWriter(new FileWriter(new File(s"kafka$k.json")))
      bw.write(kafkaTemplate.toString())
      bw.close()
      ("curl.exe -X POST -H \"Content-type: application/json\" " +
          s"$master:8080/v2/apps " +
          s"-d@kafka$k.json")!!
    }*/

  }
}
