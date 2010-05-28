package ccf.transport

trait Encoder {
  def encodeRequest(request: Request): String
  def encodeResponse(response: Response): String
}
