package task

object Container{
  implicit def dockerContainer2Option(docker: DockerContainer) = Some(docker)
  implicit def container2Option(container: Container) = Some(container)
}


case class Container(`type`: String,
                      docker: Option[DockerContainer],
                     portMappings: Option[Map[Int, Int]] = None)