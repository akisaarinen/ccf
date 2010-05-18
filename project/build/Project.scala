import sbt.{Project => SbtProject, _}
  
abstract class AbstractProject(info: ProjectInfo) extends DefaultProject(info) {
  def transitiveDepJars = (jars +++ Path.lazyPathFinder { dependencies.flatMap(jars(_)) }).distinct
  private def jars: PathFinder = mainDependencies.scalaJars +++ projectJar +++ managedDepJars +++ unmanagedDepJars
  private def jars(p: SbtProject): Seq[Path] = p match { case cp: AbstractProject => cp.jars.get.toList; case _ => Nil }
  private def projectJar = ((outputPath ##) / defaultJarName)
  private def managedDepJars = descendents(managedDependencyPath / "compile" ##, "*.jar")
  private def unmanagedDepJars = descendents(info.projectPath / "lib" ##, "*.jar")
}

class Project(info: ProjectInfo) extends ParentProject(info) { rootProject =>
  lazy val lib = project("ccf", "ccf", new CcfLibraryProject(_))
  lazy val app = project("app", "app", new TextAppProject(_), lib)
  lazy val perftest = project("perftest", "perftest", new PerftestProject(_), lib)

  class CcfLibraryProject(info: ProjectInfo) extends AbstractProject(info) {
    override def mainClass = Some("TestMain")

    val testScopeDependency = "test"

    val databinder_net = "databinder.net repository" at "http://databinder.net/repo"
    val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.7.3"
    val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.0.1"
    val specs = "org.scala-tools.testing" % "specs" % "1.6.0" % testScopeDependency
    val mockito = "org.mockito" % "mockito-core" % "1.8.0" % testScopeDependency
    val scalacheck = "org.scala-tools.testing" % "scalacheck" % "1.5" % testScopeDependency
  }

  class TextAppProject(info: ProjectInfo) extends AbstractProject(info) {
    override def mainClass = Some("textapp.TextAppMain")

    val databinder_net = "databinder.net repository" at "http://databinder.net/repo"
    val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.6.3"
    val dispatchJson = "net.databinder" %% "dispatch-json" % "0.6.3"
    val dispatchHttpJson = "net.databinder" %% "dispatch-http-json" % "0.6.3"
    val liftJson = "net.liftweb" % "lift-json" % "1.1-M5"
    val jGoodiesForms = "com.jgoodies" % "forms" % "1.2.0"
  }

  class PerftestProject(info: ProjectInfo) extends AbstractProject(info) {
    override def manifestClassPath = Some(distFileJars.map(_.getName).mkString(" "))
    override def mainClass = Some("perftest.Perftest")

    val jetty7 = "org.eclipse.jetty" % "jetty-webapp" % "7.1.0.RC0"

    lazy val dist = zipTask(transitiveDepJars, "dist", distName) dependsOn (`package`)

    private def distName = "%s-%s.zip".format(name, version)
    private def distFileJars = transitiveDepJars.getFiles.filter(_.getName.endsWith(".jar"))
  }
}
