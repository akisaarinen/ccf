package ccf.messaging

import org.specs.Specification
import ccf.tree.operation.{TreeOperationDecoder, NoOperation}

class MessageSpec extends Specification {
  val operationDecoder = TreeOperationDecoder
  "Message factory method with default decoder" should {
    "create operation context from map" in {
      val operationContext =  OperationContext(new NoOperation, 1, 2)
      Message(operationContext.encode.asInstanceOf[Map[String, String]], operationDecoder) must equalTo(operationContext)
    }
    "create error messsage from map" in {
      val errorMessage = ErrorMessage("not so critical error")
      Message(errorMessage.encode.asInstanceOf[Map[String, String]], operationDecoder) must equalTo(errorMessage)
    }
    "create channel shutdown from map" in {
      val channelShutdown = ChannelShutdown("critical error")
      Message(channelShutdown.encode.asInstanceOf[Map[String, String]], operationDecoder) must equalTo(channelShutdown)
    }
    "throw exception for unknown type" in {
      Message(Map("abc" -> "123"), operationDecoder) must throwAn[Exception]
    }
  }

  "OperationContext" should {
    "be encoded to and from map of strings" in {
      val operation = new NoOperation()
      val operationContext = OperationContext(operation, 1, 2)
      val encodedOperation: Map[String, String] = operationContext.encode.asInstanceOf[Map[String, String]]
      OperationContext(encodedOperation) must equalTo(operationContext)
    }
  }
   "Error message" should {
     "be encodable" in {
       val errorMessage = ErrorMessage("some kind of error")
       ErrorMessage(errorMessage.encode) must equalTo(errorMessage)
     }
   }
   "Shutdown message" should {
    "be encodable" in {
      val shutdownMessage = ChannelShutdown("shut it down")
      ChannelShutdown(shutdownMessage.encode) must equalTo(shutdownMessage)
    }
  }
}