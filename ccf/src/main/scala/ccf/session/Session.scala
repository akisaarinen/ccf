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

package ccf.session

import ccf.transport.Connection

case class Session(connection: Connection, version: Version, clientId: ClientId, seqId: Int, channels: Set[ChannelId]) {
  def next(channels: Set[ChannelId]) = Session(connection, version, clientId, seqId + 1, channels)

  def send(msg: Message): (Session, Either[Failure, Success]) = handleException(msg) { msg.send(this) match {
    case (session: Session, Some(response)) => response.result match {
      case Right(Success(_, result))  => success(session, msg, result)
      case Left(Failure(_, reason)) => failure(msg, reason)
    }
    case (session: Session, None)           => success(session, msg, None)
    case _                                  => error("Session got invalid response from #send")
  }}

  private def handleException(msg: Message)(block: => (Session, Either[Failure, Success])) =
    try { block } catch { case e: Exception => failure(msg, e.toString) }

  private def success(s: Session, m: Message, result: Option[Any]) = (s, Right(Success(m, result)))
  private def failure(msg: Message, reason: String) = (this, Left(Failure(msg, reason)))
}
