object TestMain extends App {

  val raspi = Node("10.0.0.13", "pi", "raspi-server")
  raspi.executeScript(Scripts.TEST)

}
