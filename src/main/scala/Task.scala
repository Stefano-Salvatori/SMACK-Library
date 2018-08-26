import MarathonTask.Container

trait Task {

  var id: String

  val cpus: Double

  val mem: Double

  val cmd: Option[String]

  val container: Option[Container]

  var env: Map[String, String]

  var instances: Int

  def saveAsJson(): Unit

}
