/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt.{Project => SbtProject, _}
import de.element34.sbteclipsify._

abstract class AbstractProject(info: ProjectInfo) extends DefaultProject(info) with IdeaProject with Eclipsify {
  def transitiveDepJars = (jars +++ Path.lazyPathFinder { dependencies.flatMap(jars(_)) }).distinct
  private def jars: PathFinder = mainDependencies.scalaJars +++ projectJar +++ managedDepJars +++ unmanagedDepJars
  private def jars(p: SbtProject): Seq[Path] = p match { case cp: AbstractProject => cp.jars.get.toList; case _ => Nil }
  private def projectJar = ((outputPath ##) / defaultJarName)
  private def managedDepJars = descendents(managedDependencyPath / "compile" ##, "*.jar")
  private def unmanagedDepJars = descendents(info.projectPath / "lib" ##, "*.jar")
  override def compileOptions = super.compileOptions ++ Seq(Unchecked)
}

class Project(info: ProjectInfo) extends ParentProject(info) with IdeaProject { rootProject =>
  lazy val lib = project("ccf", "ccf", new CcfLibraryProject(_))
  lazy val app = project("app", "app", new TextAppProject(_), lib)
  lazy val perftest = project("perftest", "perftest", new PerftestProject(_), lib)

  class CcfLibraryProject(info: ProjectInfo) extends AbstractProject(info) {
    override def mainClass = Some("TestMain")

    val testScopeDependency = "test"

    val databinder_net = "databinder.net repository" at "http://databinder.net/repo"
    val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.7.4"
    val httpclient = "org.apache.httpcomponents" % "httpclient" % "4.0.1"
    val specs = "org.scala-tools.testing" % "specs_2.8.0" % "1.6.5" % testScopeDependency
    val mockito = "org.mockito" % "mockito-core" % "1.8.4" % testScopeDependency
    val scalacheck = "org.scala-tools.testing" % "scalacheck" % "1.5" % testScopeDependency
  }

  class TextAppProject(info: ProjectInfo) extends AbstractProject(info) {
    override def mainClass = Some("textapp.TextAppMain")

    val databinder_net = "databinder.net repository" at "http://databinder.net/repo"
    val dispatchHttp = "net.databinder" %% "dispatch-http" % "0.7.4"
    val dispatchJson = "net.databinder" %% "dispatch-json" % "0.7.4"
    val dispatchHttpJson = "net.databinder" %% "dispatch-http-json" % "0.7.4"
    val liftJson = "net.liftweb" % "lift-json_2.8.0" % "2.1-M1"
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
