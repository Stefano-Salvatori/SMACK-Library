package task

object Container {
  implicit def dockerContainer2Option(docker: DockerContainer) = Some(docker)

  implicit def container2Option(container: Container) = Some(container)
}

/**
  * A container is an executable package of software that includes everything needed to run an
  * application
  * @param `type`
  *               container type (e.g DOCKER or MESOS)
  * @param docker
  *               an optional that must be used if container type is DOCKER
  * @param portMappings
  *                     port mapping from host machine to container
  */
case class Container(`type`: String,
                     docker: Option[DockerContainer],
                     portMappings: Option[Map[Int, Int]] = None){
  if(`type`=="DOCKER" && docker.isEmpty) {
    throw new IllegalStateException("You must define a DockerContainer if container type is DOCKER")
  }
}