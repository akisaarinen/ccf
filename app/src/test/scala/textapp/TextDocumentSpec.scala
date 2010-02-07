package textapp

import org.specs.Specification
import ccf.tree.operation._
import ccf.tree.indexing._

object TextDocumentSpec extends Specification {
  "Empty TextDocument" should {
    val doc = new TextDocument("")
    "be empty initially" in {
      doc.text must equalTo("")
    }
    "calculate initial MD5 hash correctly" in {
      doc.hash must equalTo("d41d8cd98f00b204e9800998ecf8427e")
    }
    "ignore no-ops" in {
      doc.applyOp(NoOperation())
      doc.text must equalTo("")
    }
  }

  "TextDocument with operations applied" should {
    val doc = new TextDocument("")
    doBefore {
      doc.applyOp(new InsertOperation(TreeIndex(0), Elem('a')))
      doc.applyOp(new InsertOperation(TreeIndex(1), Elem('b')))
      doc.applyOp(new InsertOperation(TreeIndex(2), Elem('c')))
      doc.applyOp(new InsertOperation(TreeIndex(3), Elem('d')))
      doc.applyOp(new DeleteOperation(TreeIndex(0)))
    }
    
    "have correct text" in {
      doc.text must equalTo("bcd")
    }

    "calculate MD5 hash correctly" in {
      doc.hash must equalTo("d4b7c284882ca9e208bb65e8abd5f4c8")
    }
  }
}
