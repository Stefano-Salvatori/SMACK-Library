#!/bin/sh
exec scala "$0" "$@"
!#

import java.io._
import java.net._
import scala.sys.process._
import scala.language.postfixOps

import scala.language.postfixOps

object Main extends App {
  val DEFAULT_HOST_FILE = s"127.0.1.1 $myHostname $myHostname\n" +
    "127.0.0.1 localhost\n\n" +
    "# The following lines are desirable for IPv6 capable hosts\n" +
    "::1 ip6-localhost ip6-loopback\n" +
    "fe00::0 ip6-localnet\n" +
    "ff00::0 ip6-mcastprefix\n" +
    "ff02::1 ip6-allnodes\n" +
    "ff02::2 ip6-allrouters\n" +
    "ff02::3 ip6-allhosts"

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

  def myHostname: String = "hostname" !!

  val myIp = ipAddress()
  writeToFile("/etc/hosts", DEFAULT_HOST_FILE, append = false)
  for (Array(ip, host) <- args.grouped(2)) {
    writeToFile("/etc/hosts", s"\n$ip $host", append = true)
  }


}

Main.main(args)

