package ccf.transport

trait Formatter {
  def format(data: Any): String
}
