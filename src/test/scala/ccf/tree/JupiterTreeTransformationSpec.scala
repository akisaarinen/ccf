package ccf.tree

import org.specs.Specification
import ccf.tree.operation._

object JupiterTreeTransformationSpec extends Specification {
  val t = JupiterTreeTransformation
  "JupiterTreeTransformation" should {
    "create no-op" in {
      t.createNoOp must equalTo(NoOperation())
    }
  }
}
