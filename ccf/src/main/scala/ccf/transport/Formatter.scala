package ccf.transport

trait Formatter {
  def format(request: Request): String
}
