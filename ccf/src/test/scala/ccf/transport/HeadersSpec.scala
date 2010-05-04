package ccf.transport

import org.specs.Specification

object HeadersSpec extends Specification {
  "Headers" should {
    val headers = Headers("a" -> "b", "c" -> "d")
    "match if their contents are equal" in {
      Headers("a" -> "b", "c" -> "d") must be equalTo(headers)
    }
    "produce same string representation as Map" in {
      val map = Map("a" -> "b", "c" -> "d")
      map.toString must be equalTo(headers.toString)
    }
  }
}
