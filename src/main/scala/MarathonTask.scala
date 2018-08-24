import java.io.{BufferedWriter, File, FileWriter}

import net.liftweb.json.Serialization.write
import net.liftweb.json._

import scala.io.Source

case object MarathonTask {
  implicit val formats: DefaultFormats.type = DefaultFormats

  case class DockerContainer(image: String,
                             network: String,
                             forcePullImage: Boolean,
                             privileged: Boolean,
                             ports: Array[Int])


  case class Container(docker: DockerContainer)

  /**
    * Get a marathonTask object from a json file
    *
    * @param json
    * the file to read
    * @return
    * the task object
    */
  def apply(json: String): MarathonTask = parse(Source.fromFile(json).getLines.mkString).extract[MarathonTask]

}


import MarathonTask.Container


case class MarathonTask(var id: String,
                        container: Option[Container],
                        var env: Map[String, String],
                        cpus: Double,
                        mem: Double,
                        cmd: Option[String],
                        var instances: Int) extends Task {

  def saveAsJson(fileName: String): Unit = {
    val bw = new BufferedWriter(new FileWriter(new File(s"$fileName.json")))
    val toWrite = prettyRender(parse(write(this)(formats = MarathonTask.formats)))
    bw.write(toWrite)
    bw.close()
  }
}