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

class GenericTask(var id: String,
                  var cpus: Double,
                  var mem: Double,
                  var disk: Double,
                  var cmd: Option[String],
                  val container: Option[Container],
                  var env: Map[String, String],
                  var instances: Int = 1) extends MarathonTask {
  this.id = id.toLowerCase
}
