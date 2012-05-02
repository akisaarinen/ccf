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

import sbt._

class Plugins(info: ProjectInfo) extends PluginDefinition(info) {
  val repo = "GH-pages repo" at "http://mpeltonen.github.com/maven/"
  val idea = "com.github.mpeltonen" % "sbt-idea-plugin" % "0.1-SNAPSHOT"
  val eclipse = "de.element34" % "sbt-eclipsify" % "0.6.0"
}
