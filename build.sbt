name := "Software-Defined-Infrastructure"

version := "0.1"

scalaVersion := "2.11.12"


libraryDependencies += "com.lihaoyi" %% "ujson" % "0.6.6"
//SSH connection library
libraryDependencies += "org.jvnet.hudson" % "ganymed-ssh-2" % "build260"

//Library to handle json
libraryDependencies += "net.liftweb" %% "lift-json-ext" % "3.3.0"
libraryDependencies += "net.liftweb" %% "lift-json" % "3.3.0"

//Spark-Cassandra connection library
libraryDependencies += "com.datastax.spark" %% "spark-cassandra-connector" % "2.3.1"

//Spark Mesos
libraryDependencies += "org.apache.spark" %% "spark-mesos" % "2.3.1"


//Spark library
libraryDependencies += "org.apache.spark" %% "spark-core" % "2.3.1"



