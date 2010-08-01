package ccf.tree.operation

import org.specs.Specification
import ccf.tree.indexing.TreeIndex
object TreeOperationCodingSpec extends Specification {
  val encoder = new TreeOperationEncoder
  val decoder = new TreeOperationDecoder {
    protected def parseNode(encodedValue: Any) = {
      TestNode(encodedValue.asInstanceOf[String])
    }

    protected def parseModifier(encodedValue: Any) = {
      TestModifier(encodedValue.asInstanceOf[String])
    }
  }

  "Encoder and decoder" should {
    val index = TreeIndex(1,2,3)
    val node = TestNode("lol")
    "encode and decode insert" in {
      val original = InsertOperation(index, node)
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode delete" in {
      val original = DeleteOperation(index)
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode move" in {
      val src = index
      val dst = TreeIndex(4,5,6)
      val original = MoveOperation(src, dst)
      encodeAndDecode(original) must equalTo(original)
    }
    "encode and decode update" in {
      val original = UpdateAttributeOperation(index, "someAttr", new TestModifier("foo"))
      encodeAndDecode(original) must equalTo(original)
    }
  }

  private def encodeAndDecode(op: TreeOperation): TreeOperation = {
    val encoded = encoder.encode(op)
    decoder.decode(encoded)
  }
}