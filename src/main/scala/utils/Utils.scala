package utils

/**
  * Utility class
  */
object Utils {
  lazy val curlCmd = {
    System.getProperty("os.name") match {
      case s if s.startsWith("Windows") => "scripts/curl.exe"
      case _ => "curl"
    }
  }
}
