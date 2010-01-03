package textapp.server

import scala.io.Source
import java.io.InputStream
import java.net.URLDecoder

class FormDecoder(inputStream: InputStream) {
  private val source = Source.fromInputStream(inputStream)
  private val sourceAsString = source.getLines.toList.foldLeft("")(_+_)
  private val paramArray = sourceAsString.split("&").filter(_ != "").map(_.split("=")).map { kvPair =>
    if (kvPair.size != 2) throw new RuntimeException("Invalid key-value pair in '%s'".format(kvPair.mkString("=")))
    val (encodedKey, encodedValue) = (kvPair(0), kvPair(1))
    val key = URLDecoder.decode(encodedKey)
    val value = URLDecoder.decode(encodedValue)
    (key, value)
  }
  val params = Map(paramArray: _*)
}

