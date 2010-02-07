package textapp.server

import com.sun.net.httpserver.{HttpServer}
import java.net.InetSocketAddress

class ServerApp(port: Int) {
  try {
    val server = HttpServer.create(new InetSocketAddress(port), 0)
    server.createContext("/textapp", new TextAppRequestHandler)
    server.setExecutor(null) // creates a default executor
    server.start()
  } catch {
    case e =>
      println("Error while starting server")
      println(e)
  }
}
