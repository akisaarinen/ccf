package ccf.session

import org.specs.Specification

object VersionSpec extends Specification {
  "Version" should {
    val version = Version(1, 2)
    "format to correct string" in {
      version.toString must equalTo("1.2")
    }
    "parse from correct version string" in {
      Version("1.2") must equalTo(Some(version))
    }
    "return None if version string is invalid" in {
      Version("") must beNone
      Version("a.b") must beNone
      Version("1.2.3") must beNone
    }
  }
}
