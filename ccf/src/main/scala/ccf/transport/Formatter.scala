package ccf.transport

trait Formatter {
  def formatRequest(request: Request): String
}
