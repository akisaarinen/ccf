package ccf.transport

trait TransportRequestInterceptor {
  def isPermitted(r: TransportRequest): (Boolean, String)
}

class DefaultTransportRequestInterceptor extends TransportRequestInterceptor {
  def isPermitted(r: TransportRequest) = (true, "")
}