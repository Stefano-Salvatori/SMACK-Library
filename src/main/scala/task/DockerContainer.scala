package task

case class Parameter(key:String, value:String)

case class DockerContainer(image: String,
                           network: String,
                           forcePullImage: Boolean,
                           privileged: Boolean,
                           ports: Array[Int],
                           parameters:Array[Parameter] = Array())
