package cluster

import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.{Files, Paths}

import ch.ethz.ssh2.{Connection, SCPClient, StreamGobbler}

object Node {

}

case class Node(private val ip: String,
                private val usr: String,
                private val keyPath: String,
                private val keyPsw: String) {

  this.checkValidIp(ip)
  lazy val keyFile = new File(keyPath)
  private var hostname: String = _


  def getIp: String = this.ip

  /**
    * Execute a script on this Machine.
    * (This function sends the script file via scp then execute it by calling the
    * [[cluster.Node#executeCommand]]("./script_name"); finally delete the file with
    * executeCommand("rm ./script_name")
    *
    * @param script
    * the [[cluster.Script]] to execute.
    * @param params
    * the param to pass as arguments of the script
    * @return
    * the result string
    */
  def executeScript(script: Script, params: String*) = {
    val scriptFile = new File(script.path)
    val conn = new Connection(this.getIp)
    conn.connect()
    conn.authenticateWithPublicKey(usr, keyFile, keyPsw)
    //conn.authenticateWithPassword(this.usr, this.psw)
    val scp = new SCPClient(conn)
    //send the file to execute via scp
    val ouputStream = scp.put(scriptFile.getName, scriptFile.length, ".", "7777")
    ouputStream.write(Files.readAllBytes(Paths.get(script.path)))
    ouputStream.close()
    conn.close()
    val result = this.executeCommand(s"./${scriptFile.getName} ${params.mkString(" ")}")
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
    var result: String = ""
    do {
      if (printResult) println(line)
      result = result.concat(line)
      line = br.readLine()
    } while (line != null)
    //Close connection
    sess.close()
    conn.close()
    result
  }

  /**
    *
    * @return the hostname of this node
    */
  def getHostname: String = {
    if (this.hostname == null) {
      this.hostname = this.executeCommand("hostname", printResult = false)
      this.hostname
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
