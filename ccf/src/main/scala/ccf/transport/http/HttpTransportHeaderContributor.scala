package ccf.transport

trait HttpTransportHeaderContributor {
  def getHeaders: Map[String, String]
}

class DefaultHttpTransportHeaderContributor extends HttpTransportHeaderContributor {
  def getHeaders = Map()
}