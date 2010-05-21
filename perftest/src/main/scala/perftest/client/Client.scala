package perftest.client

import ccf.transport.http.HttpConnection

import ccf.session.{ChannelId, ClientId, Version}
import ccf.session.{SessionActor, Join, InChannelMessage, Shutdown}

import java.net.URL

import net.lag.logging.Logger
import java.util.logging.Level

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
  private val numberOfMsgsToSend = 10000
  private val clientId = ClientId.randomId
  private val version = Version(1, 2)
  def run(url: URL) = {
    val conn = HttpConnection.create(url)
    val sa = new SessionActor(conn, clientId, version)
    sa ! Join(ChannelId.randomId)
    Logger.get("dispatch").setLevel(Level.OFF)
    report(roundTripTimes(sa))
    sa ! Shutdown
  }
  private def roundTripTimes(sa: SessionActor): List[Double] = {
    import System.currentTimeMillis
    (0 to numberOfMsgsToSend).map { _ => 
      val startTimestampMillis = currentTimeMillis
      sa !? InChannelMessage("type", ChannelId.randomId, Some("request content"))
      (currentTimeMillis - startTimestampMillis).asInstanceOf[Double]
    }.toList
  }
  private def report(xs: List[Double]) {
    import Statistics._
    println("mean=%f,sd=%f,min=%f,max=%f".format(mean(xs), stddev(xs), min(xs), max(xs)))
  }
}
