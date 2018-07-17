import java.io.{BufferedWriter, File, FileWriter}
import scala.sys.process._

import com.decodified.scalassh._
import ujson.Js

import scala.io.Source


object Main {

  def main(args: Array[String]): Unit = {
    val filename = "config-cluster.json"
    val fileContents = Source.fromFile(filename).getLines.mkString

    val json = ujson.read(fileContents)
    val master = json("masters")(0).str
    val agents = json("agents")
    val usr = json("user").str
    val psw = json("password").str
    val cassandraInstances = json("cassandra-instances").num.intValue
    val kafkaInstances = json("kafka-instances").num.intValue


    /*val config = HostConfig(PasswordLogin(usr, SimplePasswordProducer(psw)))
    SSH(master, HostConfigProvider.fromHostConfig(config)) {
      client => {
        client upload ("scripts/setup_master.sh", ".")
        client exec "chmod u+x setup_master.sh"
        client exec "./setup_master.sh"
      }
    }

    agents.arr.map(_.str)
      .foreach(SSH(_, HostConfigProvider.fromHostConfig(config)) {
        client => {
          client upload ("scripts/setup_agent.sh", ".")
          client exec "chmod u+x setup_agent.sh"
          client exec s"./setup_agent.sh $master"
        }
      })*/
    val cassandraTemplate = ujson.read(Source.fromFile("cassandra-template.json").getLines
      .mkString)

    cassandraTemplate("container")
      .obj("docker")
      .obj("parameters")(1)("value") = s"CASSANDRA_SEEDS = ${agents.arr.map(_.str).mkString(",")}"

    /*for (c <- 1 to cassandraInstances) {
      cassandraTemplate("id") = Js.JsonableString(s"/cassandra-cluster/cassandra$c")
      val bw = new BufferedWriter(new FileWriter(new File(s"cassandra$c.json")))
      bw.write(cassandraTemplate.toString())
      bw.close()
      Runtime.getRuntime
        .exec("curl.exe -X POST -H \"Content-type: application/json\" " +
          s"$master:8080/v2/apps " +
          s"-d@cassandra$c.json")
    }*/

    val kafkaTemplate = ujson.read(Source.fromFile("kafka-template.json").getLines.mkString)
    val env = kafkaTemplate("container")
      .obj("docker")
      .obj("parameters")
    env(0)("value") = s"KAFKA_ZOOKEEPER_CONNECT=$master:2181/kafka"
    for (k <- 1 to kafkaInstances) {
      val insidePort = 9093 - k
      val outsidePort = 9095 - k
      kafkaTemplate("id") = Js.JsonableString(s"/kafka-cluster/kafka$k")
      env(1)("value") = s"KAFKA_ADVERTISED_LISTENERS=INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      env(2)("value") = s"KAFKA_LISTENERS=INSIDE://:$insidePort,OUTSIDE://:$outsidePort"
      val bw = new BufferedWriter(new FileWriter(new File(s"kafka$k.json")))
      bw.write(kafkaTemplate.toString())
      bw.close()
      ("curl.exe -X POST -H \"Content-type: application/json\" " +
          s"$master:8080/v2/apps " +
          s"-d@kafka$k.json")!!
    }

  }
}
