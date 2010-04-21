package perftest.client

import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{DefaultHttpClient, BasicResponseHandler}

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
  private val httpClient = new DefaultHttpClient
  private val payload = (0 to 1023).map(x => 0).mkString("")
  def main(args: Array[String]) = {
    report(roundTripTimes(new URL(args(0))))
  }
  private def roundTripTimes(url: URL): List[Double] = {
    (0 to numberOfHttpRequests).map { _ => measure(send, url) }.toList
  }
  private def measure(measurable: URL => Unit, url: URL) = {
    val startMillis = System.currentTimeMillis
    measurable(url)
    (System.currentTimeMillis - startMillis).asInstanceOf[Double]
  }
  private def report(xs: List[Double]) {
    import Statistics._
    println("mean=%f,sd=%f,min=%f,max=%f".format(mean(xs), stddev(xs), min(xs), max(xs)))
  }
  private def send(url: URL) {
    val httpPost = new HttpPost(url.toString) {
      setEntity(new StringEntity(payload))
    }
    httpClient.execute[String](httpPost, new BasicResponseHandler)
  }
}
