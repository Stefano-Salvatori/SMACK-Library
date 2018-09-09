#!/bin/sh
exec scala "$0" "$@"
!#

import java.io._
import java.net._

import scala.sys.process._
import scala.language.postfixOps

object Main extends App {
  "sudo service mesos-master stop" !!

  "sudo service zookeeper stop" !!

  "sudo service marathon stop" !!

}

Main.main(args)

