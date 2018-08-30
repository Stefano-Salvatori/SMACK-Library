package task

object ContainerType extends Enumeration {
  type ContainerType = Value
  val DOCKER = Value("DOCKER")
  val MESOS = Value("MESOS")
}
