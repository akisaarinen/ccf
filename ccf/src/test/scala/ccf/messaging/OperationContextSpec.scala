package ccf.messaging

import org.specs.Specification
import ccf.tree.operation.{NoOperation}

class OperationContextSpec extends Specification {
  "OperationContext" should {
    "be encoded to and from map of strings" in {
      val operation = new NoOperation()
      val operationContext = OperationContext(operation, 1, 2)
      val encodedOperation: Map[String, String] = operationContext.encode.asInstanceOf[Map[String, String]]
      OperationContext(encodedOperation) must equalTo(operationContext)
    }
  }
}