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

import org.specs.Specification

class CommandLineArgumentProcessorSpec extends Specification {
  "CommandLineArgumentProcessor with empty argument list" should {
    val processor = new CommandLineArgumentProcessor(Array())

    "not contain any params" in {
      processor.getParam("any", "t") must equalTo(None)
    }
  }

  "CommandLineArgumentProcessor with two params with attached values" should {
    val args = Array("--param1", "value1", "-p2", "value2")
    val processor = new CommandLineArgumentProcessor(args)

    "find long parameter param1 with attached value" in {
      processor.getParam("param1", "p1") must equalTo(Some("value1"))
    }

    "find short parameter param2 with attached value" in {
      processor.getParam("param2", "p2") must equalTo(Some("value2"))
    }

    "not find non-existent param3" in {
      processor.getParam("param3", "p3") must equalTo(None)
    }
  }


  "CommandLineArgumentProcessor with two params without attached values" should {
    val args = Array("--server", "-x")
    val processor = new CommandLineArgumentProcessor(args)

    "find server param without value" in {
      processor.getParam("server", "s") must equalTo(Some(""))
    }

    "find short parameter x without value" in {
      processor.getParam("xxxxx", "x") must equalTo(Some(""))
    }

    "not find non-existent param3" in {
      processor.getParam("param3", "p3") must equalTo(None)
    }
  }
}