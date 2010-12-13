package ccf.server

trait StateSerializer {
   def serialize(op: AnyRef): String
   def deserialize[A](in: String)(implicit mf: scala.reflect.Manifest[A]): A
}