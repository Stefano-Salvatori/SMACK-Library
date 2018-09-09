package utils

/**
  * Utility class
  */
object Utils {
  lazy val curlCmd = {
    System.getProperty("os.name") match {
      case s if s.startsWith("Windows") => "curl.exe"
      case _ => "curl"
    }
  }
}
