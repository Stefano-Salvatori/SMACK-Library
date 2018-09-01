package cluster

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.{Files, Paths}

import ch.ethz.ssh2.{Connection, SCPClient, StreamGobbler}

object Node {
  def apply(ip: String, usr: String, keyPath: String, keyPsw: String) = {
    new Node(ip, usr, keyPath, keyPsw)
  }

}

case class Node(ip: String, usr: String, keyPath: String, keyPsw: String) {

  this.checkValidIp(ip)
  lazy val keyFile = new File(keyPath)
  private val hostname: String = null


  def getIp: String = this.ip

  def executeScript(script: Scripts, printResult: Boolean, params: String*) = {
    val scriptFile = new File(script.getPath)
    val conn = new Connection(this.getIp)
    conn.connect()
    conn.authenticateWithPublicKey(usr, keyFile, keyPsw)
    //conn.authenticateWithPassword(this.usr, this.psw)
    val scp = new SCPClient(conn)
    //send the file to execute via scp
    val ouputStream = scp.put(scriptFile.getName, scriptFile.length, ".", "7777")
    ouputStream.write(Files.readAllBytes(Paths.get(script.getPath)))
    ouputStream.close()
    conn.close()
    val result = this.executeCommand(s"./${scriptFile.getName} ${params.mkString(" ")}", printResult)
    this.executeCommand(s"rm ./${scriptFile.getName}", printResult = false)
    result
  }

  /**
    * Execute a shell command on the node
    *
    * @param command
    * the command to execute
    * @return the result of the command
    */
  def executeCommand(command: String, printResult: Boolean = true): String = {
    //Start connection
    val conn = new Connection(this.getIp)
    conn.connect
    conn.authenticateWithPublicKey(usr, keyFile, keyPsw)

    //conn.authenticateWithPassword(usr, psw)
    //Start execution session
    val sess = conn.openSession
    //Execute command
    sess.execCommand(command)
    //Print output
    val stdout = new StreamGobbler(sess.getStdout)
    val br = new BufferedReader(new InputStreamReader(stdout))
    var line: String = ""
    val result: String = ""
    do {
      if (printResult) println(line)
      result.concat(line)
      line = br.readLine()
    } while (line != null)
    //Close connection
    sess.close()
    conn.close()
    line
  }

  def getHostname: String = {
    if (this.hostname == null) {
      this.executeCommand("hostname", printResult = false)
    } else {
      this.hostname
    }
  }

  private def checkValidIp(ip: String): Unit = {
    val pattern = """.*?(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3}).*""".r
    ip match {
      case pattern(_*) => None
      case _ => throw new IllegalArgumentException(s"Invalid ip address $ip")
    }
  }
}
