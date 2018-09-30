package smack

import cluster.MesosCluster
import net.liftweb.json.JsonAST.{JField, JObject, JString}
import net.liftweb.json.parse
import smack.KafkaCluster.KafkaConnectionInfo
import task.KafkaTask
import task.KafkaTask.KafkaVariable


object KafkaCluster {

  case class KafkaConnectionInfo(brokers: Map[String, Int])

}

private class KafkaCluster(override val mesos: MesosCluster,
                           override val clusterName: String) extends MarathonCluster {

  /**
    * @return An optional with a map that associates ip address of the broker to the port they
    *         are listening on;
    *         None if the cluster doesn't exist
    */
  def getConnectionInfo: Option[KafkaConnectionInfo] = {
    mesos.getTaskInfo(this.clusterName) match {
      case Some(info) =>
        val tasks = parse(info)
        val kafkaBrokers: List[(String, Int)] =
          for {
            JObject(task) <- tasks
            JField("host", JString(ip)) <- task
          } yield ip -> 9094
        Some(KafkaConnectionInfo(kafkaBrokers.toMap))
      case None => None
    }
  }


  def run(nodes: Int, cpus: Double, memory: Double) = {
    val kafka = new KafkaTask(this.clusterName, cpus, memory, disk = 0,
                               cmd = None, instances = nodes)
    kafka.set(KafkaVariable.HOSTNAME_COMMAND,
               "ip -4 route get 8.8.8.8 | awk {'print $7'} | tr -d '\\n'")
    kafka.set(KafkaVariable.KAFKA_ZOOKEEPER_CONNECT,
               s"${mesos.masters.map(_.getIp).mkString(",")}:2181/kafka")
    kafka.set(KafkaVariable.KAFKA_LISTENERS,
               "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_ADVERTISED_LISTENERS,
               "INSIDE://_{HOSTNAME_COMMAND}:9092,OUTSIDE://_{HOSTNAME_COMMAND}:9094")
    kafka.set(KafkaVariable.KAFKA_LISTENER_SECURITY_PROTOCOL_MAP,
               "INSIDE:PLAINTEXT,OUTSIDE:PLAINTEXT")
    kafka.set(KafkaVariable.KAFKA_INTER_BROKER_LISTENER_NAME, "INSIDE")
    kafka.setEnvVariable("KAFKA_DELETE_TOPIC_ENABLE", "true")

    mesos.run(kafka)
  }


}
