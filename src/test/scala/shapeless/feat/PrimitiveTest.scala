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

import org.scalatest._
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class PrimitiveTest extends AnyFreeSpec with ScalaCheckDrivenPropertyChecks with Matchers  {
  
  "Enumerating Booleans" - {
    "checking true, false" in {
      Enumerable[Boolean].enumerate.values should contain theSameElementsInOrderAs Seq((BigInt(2), Seq(true, false)))
    }
  }
  
  "Enumerating Integers" - {
    "checking arbitrary indices" in {
      forAll { (index: BigInt) =>
        try {
          val theEnumerationAtIndex = Enumerable[Int].enumerate.index(index)
          if (index == 0) {
            theEnumerationAtIndex should equal (0)
          } else if (index % 2 == 0 && index > Int.MinValue) {
            theEnumerationAtIndex should (be < 0 and be (-1 * Enumerable[Int].enumerate.index(index - 1)))   
          } else if (index < Int.MaxValue) {
            theEnumerationAtIndex should (be >= 0 and be (Enumerable[Int].enumerate.index(index + 2) - 1))      
          }
        } catch {
          case _: IndexOutOfBoundsException =>
            index should (be < BigInt(0) or be >= -1 * BigInt(Int.MinValue) + BigInt(Int.MaxValue) + 1)
        }
      }
    }
  }
  
  "Enumerating Chars" - {
    "finding the alphabet" in {
      Enumerable[Char].enumerate.values.head._2 should contain allOf('A', 'B', ('C' to 'Z') ++ ('a' to 'z') :_*)
    }
    "checking arbitrary indicies" in {
      forAll { (index: BigInt) =>
        try {
          Enumerable[Char].enumerate.index(index) should be (index.toChar)
        } catch {
          case _: IndexOutOfBoundsException =>
            index should (be <= BigInt(0) or be >= BigInt(Char.MaxValue))
        }
      }
    }
  }
}