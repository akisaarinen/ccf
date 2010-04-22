package perftest.client

import ccf.transport.Connection
import ccf.transport.http.HttpConnection

import java.net.URL

object Statistics {
  import Math._
  def mean(xs: List[Double]) = xs.reduceLeft(_ + _) / xs.length
  def min(xs: List[Double]) = xs.reduceLeft(_ min _)
  def max(xs: List[Double]) = xs.reduceLeft(_ max _)
  def stddev(xs: List[Double]) = sqrt(variance(xs))
  def variance(xs: List[Double]) = sqDevsFromMean(xs).reduceLeft(_ + _) / (xs.length - 1)
  private def sqDevsFromMean(xs: List[Double]) = xs.map(x => pow(x - mean(xs), 2))
}

object Client {
  private val numberOfHttpRequests = 10000
  private val timeoutMillis = 1000
  private val method = "method"
  private val payload = (0 to 1023).map(x => 0).mkString("")
  def main(args: Array[String]) = {
    val url  = new URL(args(0))
    val conn = new HttpConnection(url, timeoutMillis)
    report(roundTripTimes(conn))
  }
  private def roundTripTimes(conn: Connection): List[Double] = {
    import System.currentTimeMillis
    (0 to numberOfHttpRequests).map { _ => 
      val startTimestampMillis = currentTimeMillis 
      conn.invoke(method, payload)
      (currentTimeMillis - startTimestampMillis).asInstanceOf[Double]
    }.toList
  }
  private def report(xs: List[Double]) {
    import Statistics._
    println("mean=%f,sd=%f,min=%f,max=%f".format(mean(xs), stddev(xs), min(xs), max(xs)))
  }
}
