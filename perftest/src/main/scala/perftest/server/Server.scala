package perftest.server

import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.{HttpHandler, HttpExchange}
import java.net.InetSocketAddress

import ccf.transport.Response
import ccf.transport.json.JsonFormatter

import java.net.URL

class HttpRequestHandler extends HttpHandler {
  def handle(exchange: HttpExchange) { 
    try {
      val response = Response(Map[String, String](), Some((0 to 1023).map(x => 0).mkString("")))
      val body = JsonFormatter.formatResponse(response)
      exchange.sendResponseHeaders(200, body.length)
      exchange.getResponseBody.write(body.getBytes)
    } finally {
      exchange.getResponseBody.close
    }
  }
}

object Server {
  def run(url: URL)= { 
    val server = HttpServer.create(new InetSocketAddress(url.getHost, url.getPort), 0)
    server.createContext("/perftest", new HttpRequestHandler)
    server.start
  }
}
