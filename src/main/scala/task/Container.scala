package task

import task.ContainerType.ContainerType


case class Container(`type`: ContainerType = ContainerType.DOCKER,
                     docker: Option[DockerContainer],
                     portMappings: Option[Map[Int, Int]] = None)