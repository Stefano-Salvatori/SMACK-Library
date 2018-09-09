#!/bin/sh
exec scala "$0" "$@"
!#

import java.io._
import java.net.URL

import scala.sys.process._
import scala.language.postfixOps

object Main extends App {
  def writeToFile(filePath: String, stringToWrite: String, append: Boolean): Unit = {
    val writer = new PrintWriter(new FileOutputStream(filePath, append))
    try {
      writer.write(stringToWrite)
    } finally writer.close()
  }

  def ipAddress(): String = {
    val whatismyip = new URL("http://checkip.amazonaws.com")
    val in: BufferedReader = new BufferedReader(new InputStreamReader(whatismyip.openStream()))
    in.readLine()
  }

  val myIp = ipAddress()
  val masters = args
  var zkString: String = "zk://"
  zkString = zkString.concat(masters.map(ip => ip.concat(":2181")).mkString(","))
  val mesosZk = zkString.concat("/mesos")
  val marathonsZk = zkString.concat("/marathon")
  writeToFile("/etc/default/marathon", s"MARATHON_MASTER=$mesosZk\n", append = true)
  writeToFile("/etc/default/marathon", s"MARATHON_ZK=$marathonsZk\n", append = true)
  writeToFile("/etc/default/marathon", "MARATHON_MESOS_USER=root\n", append = true)
  writeToFile("/etc/default/marathon", "MARATHON_TASK_LAUNCH_TIMEOUT=650000\n", append = true)
  writeToFile("/etc/default/marathon", s"MARATHON_HOSTNAME=$myIp\n", append = true)
  println("Starting Marathon")
  "sudo service marathon restart" !!
}
Main.main(args)

