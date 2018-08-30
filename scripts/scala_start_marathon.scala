#!/bin/sh
exec scala "$0" "$@"
!#

import java.io._

import scala.sys.process._
import scala.language.postfixOps

object Main extends App {
  def writeToFile(filePath: String, stringToWrite: String, append: Boolean): Unit = {
    val writer = new PrintWriter(new FileOutputStream(filePath, append))
    try {
      writer.write(stringToWrite)
    } finally writer.close()
  }
  val masters = args
  var zkString: String = "zk://"
  zkString = zkString.concat(masters.map(ip => ip.concat(":2181")).mkString(","))
  val mesosZk = zkString.concat("/mesos")
  val marathonsZk = zkString.concat("/marathon")
  writeToFile("/etc/default/marathon", s"MARATHON_MASTER=$mesosZk\n", append = true)
  writeToFile("/etc/default/marathon", s"MARATHON_ZK=$marathonsZk\n", append = true)
  writeToFile("/etc/default/marathon", "MARATHON_MESOS_USER=root\n", append = true)
  writeToFile("/etc/default/marathon", "TASK_LAUNCH_TIMEOUT=650000\n", append = true)
  println("Starting Marathon")
  "sudo service marathon restart" !!
}

Main.main(args)

