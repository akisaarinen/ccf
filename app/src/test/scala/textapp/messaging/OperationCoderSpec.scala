package textapp.messaging

import org.specs.Specification
import org.specs.mock.Mockito
import ccf.tree.indexing.TreeIndex
import textapp.Elem
import ccf.tree.operation.{DeleteOperation, InsertOperation}

object OperationCoderSpec extends Specification with Mockito {
  val operationCoder = new OperationCoder

  "OperationCoder" should {
    "encode and decode InsertOperation" in {
      val elem = Elem('x')
      val originalOp = new InsertOperation(TreeIndex(7), elem)
      val encodedOp = operationCoder.encode(originalOp)
      val decodedOp = operationCoder.decode(encodedOp)
      originalOp must equalTo(decodedOp)
    }

    "encode and decode DeleteOperation" in {
      val originalOp = new DeleteOperation(TreeIndex(5))
      val encodedOp = operationCoder.encode(originalOp)
      val decodedOp = operationCoder.decode(encodedOp)
      originalOp must equalTo(decodedOp)
    }
  }
}
