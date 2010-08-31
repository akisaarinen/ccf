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

package ccf

import ccf.messaging.ConcurrentOperationMessage
import tree.operation.TreeOperation

class JupiterOperationSynchronizer(val isPrimary: Boolean, transformer: JupiterTransformer) extends OperationSynchronizer {
  var lastLocallyCreatedMessage = 0
  var expectedRemoteMessage = 0
  private var unacknowledgedMessages = List[ConcurrentOperationMessage]()

  def resetToInitialState {
    lastLocallyCreatedMessage = 0
    expectedRemoteMessage = 0
    unacknowledgedMessages = List()
  }

  def createLocalOperation(operation: TreeOperation) = {
    val messageToSend = ConcurrentOperationMessage(operation, lastLocallyCreatedMessage, expectedRemoteMessage)
    unacknowledgedMessages = unacknowledgedMessages ::: List(messageToSend)
    lastLocallyCreatedMessage = lastLocallyCreatedMessage + 1
    messageToSend
  }

  def receiveRemoteOperation(remoteMessage: ConcurrentOperationMessage): TreeOperation = {
    if (remoteMessage.localMessage < expectedRemoteMessage)
      return transformer.createNoOp
    discardAcknowledgedMessages(remoteMessage.expectedRemoteMessage)

    if (remoteMessage.localMessage != expectedRemoteMessage)
      throw new RuntimeException("Missing message from the sequence, receiver expected #" + 
        expectedRemoteMessage + ", sender sent #" + remoteMessage.localMessage)

    var transformedRemoteOp = remoteMessage.op

    unacknowledgedMessages = unacknowledgedMessages.map { localMsg : ConcurrentOperationMessage =>
      val transformedLocalOp = transformLocal(localMsg.op, transformedRemoteOp)
      transformedRemoteOp = transformRemote(localMsg.op, transformedRemoteOp)
      ConcurrentOperationMessage(transformedLocalOp, localMsg.localMessage, localMsg.expectedRemoteMessage)
    }

    expectedRemoteMessage = remoteMessage.localMessage + 1
    transformedRemoteOp
  }

  private def discardAcknowledgedMessages(acknowledgedMessageIndex: Int) {
    unacknowledgedMessages = unacknowledgedMessages.filter { m  => (m.localMessage >= acknowledgedMessageIndex) }
  }

  private def transformRemote(localOp: TreeOperation, remoteOp: TreeOperation) = {
    transformer.transformRemoteOpForLocalExecution(localOp, remoteOp, isPrimary)
  }

  private def transformLocal(localOp: TreeOperation, remoteOp: TreeOperation) = {
    transformer.transformRemoteOpForLocalExecution(remoteOp, localOp, !isPrimary)
  }
}

