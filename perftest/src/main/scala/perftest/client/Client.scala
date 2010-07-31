package perftest.client

import ccf.transport.http.HttpConnection

import java.net.URL

import net.lag.logging.Logger
import java.util.logging.Level
import ccf.OperationContext
import ccf.operation.Operation
import ccf.session._
import ccf.tree.indexing.TreeIndex
import ccf.tree.TreeNode
import perftest.PerfTestTreeNode
import ccf.tree.operation._

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
  private val channelId = ChannelId.randomId
  private val version = Version(1, 2)
  def run(url: URL) = {
    val conn = HttpConnection.create(url)
    val sa = new SessionActor(conn, clientId, version)
    sa ! Join(channelId)
    Logger.get("dispatch").setLevel(Level.OFF)
    report(roundTripTimes(sa))
    sa ! Shutdown
  }
  private def roundTripTimes(sa: SessionActor): List[Double] = {
    import System.currentTimeMillis
    (0 to numberOfMsgsToSend).map { index =>
      val startTimestampMillis = currentTimeMillis
      val context = new OperationContext[TreeOperation](createOperation(index), index, 0)
      sa !? OperationContextMessage(channelId, context)
      (currentTimeMillis - startTimestampMillis).asInstanceOf[Double]
    }.toList
  }
  private def createOperation(i: Int): TreeOperation = {
    (i % 5) match {
      case 0 => NoOperation()
      case 1 => InsertOperation(TreeIndex(1,2,3), new PerfTestTreeNode("foo"))
      case 2 => DeleteOperation(TreeIndex(3,2,1))
      case 3 => MoveOperation(TreeIndex(3), TreeIndex(4))
      case 4 => UpdateAttributeOperation(TreeIndex(8), "someAttr", new NopModifier)
    }
  }
  private def report(xs: List[Double]) {
    import Statistics._
    println("mean=%f,sd=%f,min=%f,max=%f".format(mean(xs), stddev(xs), min(xs), max(xs)))
  }
}
