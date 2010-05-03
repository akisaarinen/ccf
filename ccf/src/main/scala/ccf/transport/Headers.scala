package ccf.transport

import scala.collection.immutable.TreeMap

object Headers {
  def apply(headers: (String, String)*): Headers = new Headers(TreeMap[String, String](headers:_*))
  def apply(map: Map[Any,Any]): Headers = {
    val seqOfHeaders = for ((k, v) <- map) yield (k.toString, v.toString)
    Headers(seqOfHeaders.toList: _*)
  }
}

class Headers(val headers: Map[String, String])
