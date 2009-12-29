import sbt._
import sbt.Configurations._

class CcfProject(info: ProjectInfo) extends ParentProject(info) { rootProject =>
  
  lazy val lib = project("ccf", "ccf", new CcfLibraryProject(_))

  class CcfLibraryProject(info: ProjectInfo) extends DefaultProject(info) {
    override def testFrameworks = ScalaCheckFramework :: SpecsFramework :: Nil
    override def mainClass = Some("TestMain")

    val specs = "org.scala-tools.testing" % "specs" % "1.6.0"
    val mockito = "org.mockito" % "mockito-core" % "1.8.0"
    val scalacheck = "org.scala-tools.testing" % "scalacheck" % "1.5"
    override def libraryDependencies = Set(specs, mockito, scalacheck)
  }
}
