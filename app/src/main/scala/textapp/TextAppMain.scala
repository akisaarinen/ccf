package textapp

import textapp.server.ServerApp
import textapp.client.ClientApp

object TextAppMain {
  def main(args: Array[String]) {
    if (args.contains("--server") || args.contains("-s")) { 
      new ServerApp
      println("Server application started")
    } else {
      val clientApp = new ClientApp
      println("Client application started")
    }
  }
}

