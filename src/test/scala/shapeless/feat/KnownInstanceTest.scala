/*
 * Copyright 2018-2020 Jan Bessai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shapeless.feat

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class KnownInstanceTest extends AnyFreeSpec with ScalaCheckDrivenPropertyChecks with Matchers {
  import enumerables._
  import ArbitraryInstances._
  import EnumerableInstances._
  
  implicit override val generatorDrivenConfig =
    PropertyCheckConfiguration(sizeRange = 4)
  
  "Checking known instances" - {
    "NonRec type" in {
      forAll { (x: NonRec) =>
        nonRec.enumerate.values.flatMap(_._2) should contain (x)
      }
    }
    "Rec type" in {
      forAll { (x: Rec) =>
        rec.enumerate.values.flatMap(_._2) should contain (x)
      }
    }

    def size1(m: MutualRec1): Int = {
      m match {
        case MutualRec1A() => 2 /* 1 for subtype to supertype + 1 for constructor */
        case MutualRec1B(x, y) => 2 + size2(x) + size1(y)
      }
    }
    def size2(m: MutualRec2): Int =
      m match {
        case MutualRec2A(x) => 2 + size1(x)
        case MutualRec2B() => 2
      }

    "MutualRec1 type" in {
      forAll { (x: MutualRec1) =>
        mutualRec1.enumerate.values(size1(x))._2 should contain (x)
      }
    }
    "MutualRec2 type" in {
      forAll { (x: MutualRec2) =>
        mutualRec2.enumerate.values(size2(x))._2 should contain (x)
      }
    }

    def lsize(l: Label): Int = 2 /* 1 to go from Ln to Label, 1 for the element */
    def fsize(f: Seq[Tree]): Int =
      f match {
        case t +: f => 1 /* for cons */ + tsize(t) + fsize(f)
        case _ => 1 /* for nil */
      }
    def tsize(t: Tree): Int =
      t match {
        case Node(l, xs) =>
          2 /* 1 for Node to Tree + 1 for Node */ + lsize(l) + fsize(xs)
      }
    "Tree type" in {
      forAll { (x: Tree) =>
        tree.enumerate.values(tsize(x))._2 should contain (x)
      }
    }

  }
}
