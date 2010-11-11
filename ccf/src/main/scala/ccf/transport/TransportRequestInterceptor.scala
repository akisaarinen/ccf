package ccf.server

import ccf.transport.TransportRequest

trait TransportRequestInterceptor {
  def isPermitted(r: TransportRequest): (Boolean, String)
}

class DefaultTransportRequestInterceptor extends TransportRequestInterceptor {
  def isPermitted(r: TransportRequest) = (true, "")
}