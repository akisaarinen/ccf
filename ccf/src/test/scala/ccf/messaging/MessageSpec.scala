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

package ccf.messaging

import org.specs.Specification
import ccf.tree.operation.{TreeOperationDecoder, NoOperation}

class MessageSpec extends Specification {
  val operationDecoder = TreeOperationDecoder
  "Message factory method with default decoder" should {
    "create operation context from map" in {
      val operationContext =  OperationContext(new NoOperation, 1, 2)
      Message(operationContext.encode.asInstanceOf[Map[String, String]], operationDecoder) must equalTo(operationContext)
    }
    "create error messsage from map" in {
      val errorMessage = ErrorMessage("not so critical error")
      Message(errorMessage.encode.asInstanceOf[Map[String, String]], operationDecoder) must equalTo(errorMessage)
    }
    "create channel shutdown from map" in {
      val channelShutdown = ChannelShutdown("critical error")
      Message(channelShutdown.encode.asInstanceOf[Map[String, String]], operationDecoder) must equalTo(channelShutdown)
    }
    "throw exception for unknown type" in {
      Message(Map("abc" -> "123"), operationDecoder) must throwAn[Exception]
    }
  }

  "OperationContext" should {
    "be encoded to and from map of strings using default decoder" in {
      val operation = new NoOperation()
      val operationContext = OperationContext(operation, 1, 2)
      val encodedOperation: Map[String, String] = operationContext.encode.asInstanceOf[Map[String, String]]
      OperationContext(encodedOperation, operationDecoder) must equalTo(operationContext)
    }
  }
   "Error message" should {
     "be encodable" in {
       val errorMessage = ErrorMessage("some kind of error")
       ErrorMessage(errorMessage.encode) must equalTo(errorMessage)
     }
   }
   "Shutdown message" should {
    "be encodable" in {
      val shutdownMessage = ChannelShutdown("shut it down")
      ChannelShutdown(shutdownMessage.encode) must equalTo(shutdownMessage)
    }
  }
}