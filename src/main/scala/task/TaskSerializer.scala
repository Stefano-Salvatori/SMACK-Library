package task

import net.liftweb.json._


class TaskSerializer extends Serializer[GenericTask] {
  private val Class = classOf[GenericTask]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), GenericTask] = {
    case (TypeInfo(Class, _), json) =>
      new GenericTask(json.\("id").extract[String],
                       json.\("cpus").extract[Double],
                       json.\("mem").extract[Double],
                       json.\("disk").extract[Double],
                       json.\("cmd").extract[Option[String]],
                       json.\("container").extract[Option[Container]],
                       json.\("env").extract[Map[String, String]],
                       json.\("instances").extract[Int])
  }


  import net.liftweb.json.Extraction._
  import net.liftweb.json.JsonAST._

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: GenericTask =>
      JObject(JField("id", JString(x.id)),
               JField("cpus", JDouble(x.cpus)),
               JField("mem", JDouble(x.mem)),
               JField("disk", JDouble(x.disk)),
               JField("cmd", JString(x.cmd.orNull)),
               JField("container", decompose(x.container)),
               JField("env", decompose(x.env)),
               JField("instances", JInt(x.instances)))


  }

}
