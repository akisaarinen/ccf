package textapp.messaging

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.messaging.ConcurrentOperationMessage
import ccf.tree.operation.TreeOperation

object MessageCoderSpec extends Specification with Mockito {
  val operationCoder = mock[OperationCoder]
  val mockedOp = mock[TreeOperation]
  operationCoder.encode(mockedOp) returns "<encoded op>"
  operationCoder.decode("<encoded op>") returns mockedOp
  
  val messageCoder = new MessageCoder {
    override val operationCoder = MessageCoderSpec.operationCoder
  }

  "MessageCoder" should {
    "encode and decode ConcurrentOperationMessage" in {
      val originalMsg = new ConcurrentOperationMessage(mockedOp, 123, 234)
      val encodedMsg = messageCoder.encode(originalMsg)
      val decodedMsg = messageCoder.decode(encodedMsg)
      originalMsg must equalTo(decodedMsg)
    }
  }
}
