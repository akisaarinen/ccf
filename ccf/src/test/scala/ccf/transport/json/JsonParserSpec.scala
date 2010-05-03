package ccf.transport.json

import scala.collection.immutable.TreeMap
import org.specs.Specification

object JsonParserSpec extends Specification {
  "Empty string" should {
    "parse to None" in {
      JsonParser.parse("") must equalTo(None)
    }
  }
  "Invalid JSON message" should {
    val invalidMessage = """{"a":1,"b":"c","d":3"""
    "cause MalformedDataException" in {
      JsonParser.parse(invalidMessage) must throwA[MalformedDataException]
    }
  }
  "A JSON response containing list as top-level element" should {
    "cause MalformedDataException" in {
      JsonParser.parse("[]") must throwA[MalformedDataException]
    }
  }
  "A JSON response containing improper header type" should {
    "cause MalformedDataException" in {
      JsonParser.parse("""{"headers":["foo","bar"]}""") must throwA[MalformedDataException]
    }
  }
  "A JSON response without header" should {
    "cause MalformedDataException" in {
      JsonParser.parse("""{"content":["foo","bar"]}""") must throwA[MalformedDataException]
    }
  }
  "A JSON response with headers but without content" should {
    val jsonResponse = """{"headers":{"aa":"bb","cc":"dd"}}"""
    "parse to equivalent Response" in {
      val expected = Response(Headers("aa" -> "bb", "cc" -> "dd"), None)
      val parsed = JsonParser.parse(jsonResponse).get
      parsed.headers.headers must equalTo(expected.headers.headers)
      parsed.content must equalTo(expected.content)
    }
  }
  "A JSON response with headers and content elements" should {
    val jsonResponse = """{"headers":{"aa":"bb","cc":"dd"},"content":{"b":2}}"""
    "parse to equivalent Response" in {
      val expected = Response(Headers("aa" -> "bb", "cc" -> "dd"), Some(TreeMap("b" -> 2)))
      val parsed = JsonParser.parse(jsonResponse).get
      parsed.headers.headers must equalTo(expected.headers.headers)
      parsed.content must equalTo(expected.content)
    }
  }
}
