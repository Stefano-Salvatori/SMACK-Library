package smack

import cluster.MesosCluster
import net.liftweb.json.JsonAST.{JField, JObject, JString}
import net.liftweb.json.parse
import smack.CassandraCluster.CassandraConnectionInfo
import task.CassandraTask
import task.CassandraTask.CassandraVariable

object CassandraCluster {

  case class CassandraConnectionInfo(nodes: List[String], port: Int)

}

private class CassandraCluster(override val mesos: MesosCluster, override val clusterName: String)
  extends MarathonCluster {

  /**
    * @return An optional with the list of ip addresses of this cluster with the port they are
    *         listening on;
    *         None if the cluster doesn't exist
    */
  override def getConnectionInfo = {
    mesos.getTaskInfo(this.clusterName) match {
      case Some(info) =>
        val tasks = parse(info)
        val cassandraIps = for {JObject(task) <- tasks
                                JField("host", JString(ip)) <- task} yield ip
        Some(CassandraConnectionInfo(cassandraIps, 9094))
      case None => None
    }
  }

  override def nodeTask(cpus: Double, memory: Double) = {
    val task = CassandraTask(this.clusterName, cpus, memory, 0, None)
    task.set(CassandraVariable.CASSANDRA_CLUSTER_NAME, this.clusterName)
    task.set(CassandraVariable.CASSANDRA_SEEDS, mesos.agents.map(_.getIp).mkString(","))
    task
  }
}
