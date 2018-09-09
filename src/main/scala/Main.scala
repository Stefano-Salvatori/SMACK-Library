import java.io.FileNotFoundException
import java.util.function.BiConsumer

import cluster.Script
import net.liftweb.json.JsonAST.{JField, JInt}
import net.liftweb.json.parse
import org.apache.spark.{SparkConf, SparkContext}
import task._

import scala.collection.JavaConverters._

object Main {

  def main(args: Array[String]): Unit = {
    implicit val formats = net.liftweb.json.DefaultFormats

    var kafkaBrokersCount = getAppInfo("kafka") match {
      case Some(info) =>
        (parse(info) findField {
          case JField("instances", v: JInt) => true
        }).get.value.extract[Int]
      case None => 0
    }

    println(kafkaBrokersCount)


  }

  private def getAppInfo(appId: String): Option[String] = {
    try {
      Some(scala.io.Source.fromURL(s"http://138.68.96.19:8080/v2/apps/$appId").mkString)
    } catch {
      case noApp: FileNotFoundException => None
    }
  }
}
