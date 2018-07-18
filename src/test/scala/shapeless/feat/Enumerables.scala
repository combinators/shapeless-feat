/* 
 * Copyright (c) 2015 Jan Bessai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shapeless.feat

import org.scalacheck.Gen
import org.scalacheck.Arbitrary

package enumerables {
  sealed trait NonRec
  case class NonRecA() extends NonRec
  case class NonRecB() extends NonRec
  case class NonRecC(x: Boolean) extends NonRec
    
  sealed trait Rec
  case class RecA(x: Rec) extends Rec
  case class RecB() extends Rec
  
  sealed trait MutualRec1
  sealed trait MutualRec2
  
  case class MutualRec1A() extends MutualRec1
  case class MutualRec1B(x: MutualRec2, y: MutualRec1) extends MutualRec1
  case class MutualRec2A(x: MutualRec1) extends MutualRec2
  case class MutualRec2B() extends MutualRec2
  
  sealed trait Label
  case class L1() extends Label
  case class L2() extends Label
  case class L3() extends Label
  
  sealed trait Tree
  case class Node(label: Label, children: Seq[Tree]) extends Tree
}

object EnumerableInstances {
  import enumerables._
  
  
  val nonRec = Enumerable[NonRec]
  val nonRecA = Enumerable[NonRecA]
  val nonRecB = Enumerable[NonRecB] 
  val nonRecC = Enumerable[NonRecC]
  
  val rec = Enumerable[Rec]
  val recA = Enumerable[RecA]
  val recB = Enumerable[RecB]
  
  val mutualRec1 = Enumerable[MutualRec1]
  val mutualRec1A = Enumerable[MutualRec1A]
  val mutualRec1B = Enumerable[MutualRec1B]
  val mutualRec2 = Enumerable[MutualRec2]
  val mutualRec2A = Enumerable[MutualRec2A]
  val mutualRec2B = Enumerable[MutualRec2B]
  
  val tree = Enumerable[Tree]
  val node = Enumerable[Node]
  
  val enumerableInt = Enumerable[Int]
  val enumerableChar = Enumerable[Char]
  val enumerableBoolean = Enumerable[Boolean]
  
  val enumerableListBoolean = Enumerable[List[Boolean]]
  
  val testingEnumerables: Map[Symbol, Enumerable[_ <: Any]] = Map(
      'enumerableInt -> enumerableInt,
      'enumerableChar -> enumerableChar,
      'enumerableBoolean -> enumerableBoolean,
      'enumerableListBoolean -> enumerableListBoolean,
      'nonRec -> nonRec,
      'nonRecA -> nonRecA,
      'nonRecB -> nonRecB,
      'rec -> rec,
      'recA -> recA,
      'recB -> recB,
      'mutualRec1 -> mutualRec1,
      'mutualRec1A -> mutualRec1A,
      'mutualRec1B -> mutualRec1B,
      'mutualRec2 -> mutualRec2,
      'mutualRec2A -> mutualRec2A,
      'mutualRec2B -> mutualRec2B,
      'tree -> tree,
      'node -> node
    )

  implicit lazy val arbEnumerable: Arbitrary[(Symbol, Enumerable[_ <: Any])] = 
    Arbitrary(Gen.oneOf(testingEnumerables.toSeq))
}

object ArbitraryInstances {
  import enumerables._
  
  implicit lazy val arbNonRec: Arbitrary[NonRec] =
    Arbitrary(Gen.oneOf(NonRecA(), NonRecB(), NonRecC(true), NonRecC(false)))
    
  implicit lazy val arbRec: Arbitrary[Rec] = Arbitrary[Rec] {
    def genRec(size: Int): Gen[Rec] =
      if (size <= 0) Gen.const(RecB())
      else Gen.frequency((1, Gen.const(RecB())), (3, genRec(size - 1).map(RecA(_))))
    
    Gen.sized(genRec)
  }
  
  private final def genMutualRec1(size: Int): Gen[MutualRec1] =
      if (size <= 0) Gen.const(MutualRec1A())
      else Gen.frequency(
          (1, Gen.const(MutualRec1A())), 
          (3, for { l <- genMutualRec2(size/2); r <- genMutualRec1(size/2) } yield MutualRec1B(l, r)))
  private final def genMutualRec2(size: Int): Gen[MutualRec2] =
      if (size <= 0) Gen.const(MutualRec2B())
      else Gen.frequency(
          (1, Gen.const(MutualRec2B())), 
          (3, genMutualRec1(size - 1).map(MutualRec2A(_))))
          
  implicit lazy val arbMutualRec1: Arbitrary[MutualRec1] = Arbitrary(Gen.sized(genMutualRec1))
  implicit lazy val arbMutualRec2: Arbitrary[MutualRec2] = Arbitrary(Gen.sized(genMutualRec2))
  
  implicit lazy val arbTree: Arbitrary[Tree] = Arbitrary[Tree] {
    def genTree(size: Int): Gen[Tree] = 
      for {
        label <- Gen.oneOf(L1(), L2(), L3())
        n <- Gen.choose(0, Math.max(0, Math.min(size, 3)))
        children <- if (n > 0) Gen.listOfN(n, genTree(size / 3)) else Gen.const(List())
      } yield Node(label, children)
    Gen.sized(genTree)
  }
}