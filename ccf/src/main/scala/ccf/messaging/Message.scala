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

import ccf.operation.Operation

class Message[T <: Operation]
case class ConcurrentOperationMessage[T <: Operation](val op: T, val localMessage: Int, val expectedRemoteMessage: Int) extends Message[T]
class ErrorMessage[T <: Operation](val reason: String) extends Message[T]
case class ChannelShutdown[T <: Operation](override val reason: String) extends ErrorMessage[T](reason)
