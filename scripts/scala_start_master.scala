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

  val clusterName: String = args(0)
  val masters: Array[String] = args.drop(1)
  val myIp: String = ipAddress()
  var zkString: String = "zk://"
  zkString = zkString.concat(masters.map(ip => ip.concat(":2181")).mkString(","))
  zkString = zkString.concat("/mesos")
  println(s"zkString: $zkString")
  writeToFile("/etc/mesos/zk", zkString, append = false)

  var zkServerString: String = ""
  masters.foreach(ip =>
    zkServerString = zkServerString.concat(s"server.${masters.indexOf(ip) + 1}=$ip:2888:3888\n"))
  println(s"zk server: $zkServerString")
  writeToFile("/etc/zookeeper/conf/zoo.cfg", zkServerString, append = true)

  val myId = masters.indexOf(myIp) + 1
  println(s"id: $myId")
  writeToFile("/etc/zookeeper/conf/myid", s"$myId\n", append = false)

  val quorum: Int = masters.length / 2 + 1
  println(s"quorum: $quorum")
  //writeToFile("/etc/default/mesos-master", s"MESOS_QUORUM=$quorum\n", append = true)
  writeToFile("/etc/mesos-master/quorum", s"$quorum\n", append = false)
  writeToFile("/etc/mesos-master/ip", s"$myIp\n", append = false)
  writeToFile("/etc/mesos-master/hostname", s"$myIp\n", append = false)
  writeToFile("/etc/mesos-master/cluster", s"$clusterName\n", append = false)
  println(s"Starting mesos master on $myIp")


  "sudo service zookeeper restart" !

  "sudo service mesos-master restart" !


}

Main.main(args)

