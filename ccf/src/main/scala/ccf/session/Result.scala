package ccf.session

case class Success(message: Message, result: Option[Any])
case class Failure(message: Message, reason: String)
