package ccf.messaging

import org.specs.Specification
import ccf.tree.operation.{NoOperation}

class MessageSpec extends Specification {
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
       errorMessage.encode must equalTo(Map("type" -> "error", "reason" -> "some kind of error"))
     }
   }
   "Shutdown message" should {
    "be encodable" in {
       val shutdownMessage = ChannelShutdown("shut it down")
       shutdownMessage.encode must equalTo(Map("type" -> "shutdown", "reason" -> "shut it down"))
    }
  }
}