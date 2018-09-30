package task

import task.DockerContainer.DockerParameter

object DockerContainer {

  case class DockerParameter(key: String, value: String)

}

/**
  * @param image
  * docker image to use for this container
  * @param network
  * network mode (HOST or BRIDGE)
  * @param forcePullImage
  * if true, the docker image will be updated every time, even if it
  * already exists in the host
  * @param privileged
  * Give extended privileges to this container
  * @param ports
  * the list of ports that the container exposes
  * @param parameters
  * additional parameters for docker images. for informations see
  * [[https://docs.docker.com/engine/reference/commandline/cli/#examples]]
  */
case class DockerContainer(image: String,
                           network: String,
                           forcePullImage: Boolean,
                           privileged: Boolean,
                           ports: Array[Int],
                           parameters: Array[DockerParameter] = Array())
