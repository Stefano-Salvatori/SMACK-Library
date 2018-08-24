import java.io.{BufferedReader, File, InputStreamReader}

import ch.ethz.ssh2.{Connection, StreamGobbler}
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
    val scriptName = new File(script.getPath).getName
    SSH(this.ip, HostConfigProvider.fromHostConfig(connectionConfig)) {
      client => {
        client upload(script.getPath, ".")
        client exec s"chmod u+x $scriptName"
      }
    }
    this.executeCommand(s"./$scriptName ${params.mkString(" ")}; rm ./$scriptName")
  }


  def executeCommand(command: String): Unit = {
    //Start connection
    val conn = new Connection(this.getIp)
    conn.connect
    conn.authenticateWithPassword(usr, psw)
    //Start execution session
    val sess = conn.openSession
    //Execute command
    sess.execCommand(command)
    //Print output
    val stdout = new StreamGobbler(sess.getStdout)
    val br = new BufferedReader(new InputStreamReader(stdout))
    var line: String = ""
    do{
      println(line)
      line = br.readLine()
    } while (line != null)
    //Close connection
    sess.close()
    conn.close()
  }
}
