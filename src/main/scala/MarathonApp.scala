object MarathonApp {
  case class DockerContainer(image: String,
                             network: String,
                             forcePullImage: Boolean,
                             privileged: Boolean,
                             ports: Array[Int])


  case class Container(docker: DockerContainer)

}


import MarathonApp.Container


case class MarathonApp(var id: String,
                       container: Container,
                       var env: Map[String, String],
                       cpus: Double,
                       mem: Double,
                       instances: Int)