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

package textapp.messaging

import ccf.tree.indexing.TreeIndex
import ccf.tree.operation.{TreeOperation, InsertOperation, DeleteOperation}
import scala.util.matching.Regex
import textapp.Elem

class OperationCoder {
  def encode(op: TreeOperation): String = op match {
    case InsertOperation(TreeIndex(i), Elem(c)) => "ins_%d_%d".format(i, c.toInt)
    case DeleteOperation(TreeIndex(i)) => "del_%d".format(i)
    case _ => error("no encoding for operation '%s' available".format(op.toString))
  }

  def decode(s: String): TreeOperation = {
    val InsExpr = new Regex("""ins_(\d+)_(\d+)""")
    val DelExpr = new Regex("""del_(\d+)""")
    s match {
      case InsExpr(i, c) => InsertOperation(TreeIndex(i.toInt), Elem(c.toInt.toChar))
      case DelExpr(i) => DeleteOperation(TreeIndex(i.toInt))
      case _ => error("no decoding for string '%s' available".format(s))
    }
  }
}
