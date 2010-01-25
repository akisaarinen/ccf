import sbt._
import sbt.Configurations._

class CcfProject(info: ProjectInfo) extends ParentProject(info) { rootProject =>
  lazy val lib = project("ccf", "ccf", new CcfLibraryProject(_))
  lazy val app = project("app", "app", new TextAppProject(_), lib)

  class CcfLibraryProject(info: ProjectInfo) extends DefaultProject(info) {
    override def testFrameworks = ScalaCheckFramework :: SpecsFramework :: Nil
    override def mainClass = Some("TestMain")

    val specs = "org.scala-tools.testing" % "specs" % "1.6.0"
    val mockito = "org.mockito" % "mockito-core" % "1.8.0"
    val scalacheck = "org.scala-tools.testing" % "scalacheck" % "1.5"
    override def libraryDependencies = Set(specs, mockito, scalacheck)
  }

  class TextAppProject(info: ProjectInfo) extends DefaultProject(info) {
    override def testFrameworks = SpecsFramework :: Nil
    override def mainClass = Some("textapp.TextAppMain")

    val specs = "org.scala-tools.testing" % "specs" % "1.6.0"
    val mockito = "org.mockito" % "mockito-core" % "1.8.0"
    val databinder_net = "databinder.net repository" at "http://databinder.net/repo"
    val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.6.3"
    val dispatchJson = "net.databinder" %% "dispatch-json" % "0.6.3"
    val dispatchHttpJson = "net.databinder" %% "dispatch-http-json" % "0.6.3"
    val liftJson = "net.liftweb" % "lift-json" % "1.1-M5"

    override def libraryDependencies = Set(specs, mockito, dispatchHttp, dispatchHttpJson, dispatchJson, liftJson)
  }
}
