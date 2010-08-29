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

package textapp


class CommandLineArgumentProcessor(args: Array[String]) {
  def getParam(longName: String, shortName: String): Option[String] = {
    val longFormat = "--%s".format(longName)
    val shortFormat = "-%s".format(shortName)

    val paramsWithValues = argPairs.flatMap { case (first, second) =>
      if (first == longFormat || first == shortFormat) {
        if (!isParam(second)) Some(second)
        else None
      }
      else None
    }.toList

    val paramsWithoutValues = args.flatMap { arg =>
      if (arg == longFormat || arg == shortFormat) Some("")
      else None
    }.toList
    (paramsWithValues ::: paramsWithoutValues).headOption
  }

  private def argPairs = args.zip(args.drop(1))
  private def isParam(s: String) = s.startsWith("-")
}