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

import scala.actors.Actor
import ccf.transport.Connection

class SessionActor(connection: Connection, clientId: ClientId, version: Version, session: Session) extends Actor {
  start
  def this(connection: Connection, clientId: ClientId, version: Version) = {
    this(connection, clientId, version, Session(connection, version, clientId, 0, Set()))
  }
  def act { loop(session) }
  private def loop(s: Session) { react {
    case msg: Message => {
      val nextSession = handleMessage(s, msg)
      loop(nextSession)
    }
    case Message.Shutdown => {
      sender ! s
      exit
    }
    case m => {
      error("Unhandled message " + m)
    }
  }}
  private def handleMessage(session: Session, msg: Message): Session = {
    val (nextSession, result) = session.send(msg)
    sender ! result
    nextSession
  }
}
