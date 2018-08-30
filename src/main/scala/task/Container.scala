package task

import task.ContainerType.ContainerType

import scala.collection.mutable


case class Container(`type`: ContainerType = ContainerType.DOCKER,
                     docker: Option[DockerContainer],
                     portMappings: Option[mutable.Map[Int, Int]] = None)