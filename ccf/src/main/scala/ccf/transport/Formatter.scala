package ccf.transport

trait Formatter {
  def formatRequest(request: Request): String
  def formatResponse(response: Response): String
}
