import cluster._
import net.liftweb.json.parse
import smack.{SmackEnvironment, SmackEnvironmentBuilder}
import task.CassandraTask.CassandraVariable
import task.KafkaTask.KafkaVariable
import task._

import scala.io.Source

object Main {

  def main(args: Array[String]): Unit = {
    val mesos = new MesosClusterBuilder()
      .setClusterName("Mesos-Cluster")
      .setMasters(List("104.248.23.191"))
      .setAgents(List("104.248.19.17", "104.248.23.105", "104.248.23.144"))
      .setConnection("root", "private_key_openssh", "")
      .build()

    mesos.startCluster()

  }
}
