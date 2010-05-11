package ccf.session

case class Version(major: Int, minor: Int) {
  override def toString = "%d.%d".format(major, minor)
}
object Version {
  def apply(s: String): Option[Version] = s match {
    case VersionString(minor, major) => Some(Version(minor.toInt, major.toInt))
    case _                           => None
  }
  private val VersionString = """([0-9]+)\.([0-9]+)""".r
}
