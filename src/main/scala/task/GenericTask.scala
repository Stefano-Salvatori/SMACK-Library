package task

import java.io.{BufferedReader, FileReader}

import net.liftweb.json.Serialization
import net.liftweb.json.ext.EnumNameSerializer

object GenericTask {
  def loadFromJson(json: String): GenericTask = {
    implicit val formats = net.liftweb.json.DefaultFormats +
      new TaskSerializer()
    Serialization.read[GenericTask](new BufferedReader(new FileReader(json)))
  }

}

/**
  * A generic task
  * @param id
  *           the task ID
  * @param cpus
  *             number of CPUS
  * @param mem
  *            RAM
  * @param disk
  *             space on disk needed for this task
  * @param cmd
  *            the bash command to execute
  * @param container
  *                  the container to execute
  * @param env
  *            map key-> value fro environment values
  * @param instances
  *                  number of instances that has to be launched
  */
class GenericTask(var id: String,
                  var cpus: Double,
                  var mem: Double,
                  var disk: Double,
                  var cmd: Option[String],
                  val container: Option[Container],
                  var env: Map[String, String],
                  var instances: Int = 1) extends Task {
  this.id = id.toLowerCase
}
