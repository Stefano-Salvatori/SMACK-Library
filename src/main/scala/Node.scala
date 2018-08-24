import java.io.{BufferedReader, File, InputStreamReader}
import java.nio.file.{Files, Paths}

import ch.ethz.ssh2.{Connection, SCPClient, StreamGobbler}

object Node {
  def apply(ip: String, usr: String, psw: String) = new Node(ip, usr, psw)
}

case class Node(ip: String, usr: String, psw: String) {

  def getIp: String = this.ip


  def executeScript(script: Scripts, params: String*) = {
    val scriptFile = new File(script.getPath)
    val conn = new Connection(this.getIp)
    conn.connect()
    conn.authenticateWithPassword(this.usr, this.psw)
    val scp = new SCPClient(conn)
    val ouputStream = scp.put(scriptFile.getName, scriptFile.length, ".", "7777")
    ouputStream.write(Files.readAllBytes(Paths.get(script.getPath)))
    ouputStream.close()
    conn.close()
    this.executeCommand(s"./${scriptFile.getName} ${params.mkString(" ")}")
    this.executeCommand(s"rm ./${scriptFile.getName}")
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
    do {
      println(line)
      line = br.readLine()
    } while (line != null)
    //Close connection
    sess.close()
    conn.close()
  }
}
