package perftest

import perftest.client.Client
import perftest.server.Server

import java.net.URL

object Perftest {
  private def usage = println("Usage: Perftest [client|server] URL")
  private def parse(args: Array[String]) = if (args.length > 1) Some(args(0), new URL(args(1))) else None
  def main(args: Array[String]) = parse(args) match {
    case Some(("server", url)) => Server.run(url)
    case Some(("client", url)) => Client.run(Array(url.toString))
    case None                  => usage
  }
}
