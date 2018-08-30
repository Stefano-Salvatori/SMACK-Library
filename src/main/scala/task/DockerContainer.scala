package task

case class DockerContainer(image: String,
                           network: String,
                           forcePullImage: Boolean,
                           privileged: Boolean,
                           ports: Array[Int])
