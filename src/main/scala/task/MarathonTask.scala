package task

import java.io._

import net.liftweb.json.Serialization
import net.liftweb.json.ext.EnumNameSerializer


trait MarathonTask {

  var id: String

  var cpus: Double

  var mem: Double

  var disk: Double

  var cmd: Option[String]

  val container: Option[Container]

  var env: Map[String, String]

  var instances: Int

  def setEnvVariable(name: String, value: String): Unit = {
    this.env += name -> value
  }

  def saveAsJson(): Unit = {
    implicit val formats = net.liftweb.json.DefaultFormats + new TaskSerializer()
    val bw = new BufferedWriter(new FileWriter(new File(s"$id.json")))
    val w = Serialization.writePretty(this)
    bw.write(w)
    bw.close()
  }



}
