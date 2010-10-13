package ccf.transport

import sun.misc.{BASE64Encoder, BASE64Decoder}
import java.io._

object BASE64EncodingSerializer {
  def serialize(op: AnyRef) = {
    encode(JavaSerializer.serialize(op))
  }

  def deserialize[A](in: String): A = {
    JavaSerializer.deserialize[A](decode(in))
  }

  def encode(serialized: Array[Byte]): String = new BASE64Encoder().encode(serialized)

  def decode(encoded: String): Array[Byte] = new BASE64Decoder().decodeBuffer((encoded))
}

object JavaSerializer {
  def serialize(op: AnyRef): Array[Byte] = {
    val byteStream = new ByteArrayOutputStream()
    val outputStream = new ObjectOutputStream(new BufferedOutputStream(byteStream))
    outputStream.writeObject(op)
    outputStream.close
    byteStream.toByteArray
  }

  def deserialize[A](in: Array[Byte]): A = {
    val inputStream = new ObjectInputStream(new BufferedInputStream(new ByteArrayInputStream(in)))
    inputStream.readObject.asInstanceOf[A]
  }
}