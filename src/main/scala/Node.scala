import java.io.File

import com.decodified.scalassh._
import net.schmizz.sshj.transport.verification.PromiscuousVerifier

object Node {
  def apply(ip: String, usr: String, psw: String) = new Node(ip, usr, psw)
}

case class Node(ip: String, usr: String, psw: String) {
  lazy val connectionConfig = HostConfig(PasswordLogin(usr, SimplePasswordProducer(psw)),
                                          hostKeyVerifier = new PromiscuousVerifier())

  def getIp: String = this.ip


  def executeScript(script: Scripts, params: String*) = {
    SSH(this.ip, HostConfigProvider.fromHostConfig(connectionConfig)) {
      client => {
        val scriptName = new File(script.getPath).getName
        client upload(script.getPath, ".")
        client exec s"chmod u+x $scriptName"
        for {
          result <- client exec s"./$scriptName ${params.mkString(" ")}"
        } println(result.stdOutAsString())
        client exec s"rm ./$scriptName"
      }
    }
  }


  def executeCommand(command: String): Unit = {
    SSH(this.ip, HostConfigProvider.fromHostConfig(connectionConfig)) {
      client =>
        for {
          result <- client exec command
        } println(result.stdOutAsString())
    }
  }
}
