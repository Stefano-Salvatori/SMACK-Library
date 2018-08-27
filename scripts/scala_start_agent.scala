#!/bin/sh
exec scala "$0" "$@"
!#

import java.io._
import java.net._

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
  "sudo service mesos-master stop" !

  "sudo service mesos-slave stop" !

  //sudo mkdir -p /tmp/spark-events
  val myIp =ipAddress()
  val masters = args
  //sudo hostname $my_ip

  var zkString = "zk://"
  zkString = zkString.concat(masters.map(ip => ip.concat(":2181")).mkString(","))
  zkString = zkString.concat("/mesos")
  writeToFile("/etc/mesos/zk", s"$zkString\n", append = false)

  writeToFile("/etc/mesos-slave/ip", s"$myIp\n", append = false)
  writeToFile("/etc/mesos-slave/hostname", s"$myIp\n", append = false)
  writeToFile("/etc/mesos-slave/containerizers", "docker,mesos", append = false)
  writeToFile("/etc/mesos-slave/executor_registration_timeout", "10mins", append = false)
  new File("/var/lib/mesos/meta/slaves/latest").delete()
  println(s"Starting mesos agent on $myIp")

  "sudo service mesos-slave start" !

  //_______________

}

Main.main(args)

