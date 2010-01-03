package textapp.server

import org.specs.Specification
import org.specs.mock.Mockito
import java.io.{ByteArrayInputStream}

object FormDecoderSpec extends Specification with Mockito {
  "FormDecoder" should {
    "return empty map for empty string" in {
      decode("") must equalTo(Map.empty)
    }
    "decode a single key-value pair" in {
      decode("key=value") must equalTo(Map("key" -> "value"))
    }
    "decode two key-value pairs" in {
      decode("key=value&foo=bar") must equalTo(Map("key" -> "value", "foo" -> "bar"))
    }
    "decode encoded characters" in {
      decode("a%20string=me%26my") must equalTo(Map("a string" -> "me&my"))
    }
    "throw exception if key-value pair contains too many equal-signs" in {
      decode("aaa=bbb=ccc") must throwAn[Exception]
    }
    "throw exception if key-value pair contains no equal-sign" in {
      decode("aaa") must throwAn[Exception]
    }
  }

  private def decode(s: String) = {
    val stream = new ByteArrayInputStream(s.getBytes)
    new FormDecoder(stream).params
  }
}

