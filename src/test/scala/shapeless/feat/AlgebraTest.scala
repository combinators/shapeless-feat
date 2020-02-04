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
import scala.util.Try
import org.scalatest._
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

class AlgebraTest extends FreeSpec with ScalaCheckDrivenPropertyChecks with Matchers with MatcherUtil  {
  import EnumerableInstances._

  implicit val smallBigInt = Arbitrary(Gen.choose[Int](Int.MinValue, generatorDrivenConfig.sizeRange).map(BigInt(_)))

  "Checking algebraic properties" - {
    "index and pay" in {
      forAll { (e: (Tag, Enumerable[_ <: Any]), i: BigInt) =>
         e._2.enumerate should equalOrExceptAtIndex(i)(e._2.enumerate.pay)
      }
    }
    "index injective" in {
      forAll { (e: (Tag, Enumerable[_ <: Any])) =>
        val allElements =
          (0 to generatorDrivenConfig.sizeRange)
            .map (idx => Try(e._2.enumerate.index(idx)))
            .filter (_.isSuccess)
            .map (_.get)
        allElements.size should equal (allElements.toSet.size)
      }
    }
    "pay sum distributive" in {
      forAll { (e1: (Tag, Enumerable[_ <: Any]), e2: (Tag, Enumerable[_ <: Any]), i: BigInt) =>
        e1._2.enumerate.union(e2._2.enumerate).pay should
          equalOrExceptAtIndex[Any](i)(e1._2.enumerate.pay.union(e2._2.enumerate.pay))
      }
    }
    "pay product distributive" in {
      forAll { (e1: (Tag, Enumerable[_ <: Any]), e2: (Tag, Enumerable[_ <: Any]), i: BigInt) =>
        e1._2.enumerate.product(e2._2.enumerate).pay should 
          (equalOrExceptAtIndex[(Any, Any)](i)(e1._2.enumerate.pay.product(e2._2.enumerate)) and
           equalOrExceptAtIndex[(Any, Any)](i)(e1._2.enumerate.product(e2._2.enumerate.pay)))
      }
    }
    "map preserves composition" in {
      forAll { (e: (Tag, Enumerable[_ <: Any]), i: BigInt) =>
        e._2.enumerate.map(_.toString).map(_.hashCode) should
          equalOrExceptAtIndex(i)(e._2.enumerate.map(_.toString.hashCode))
      }
    }
    "map lifts product right" in {
      forAll { (e: (Tag, Enumerable[_ <: Any]), x: Boolean, i: BigInt) =>
        e._2.enumerate.product(Enumeration.singleton(x)) should
          equalOrExceptAtIndex[(Any, Any)](i)(e._2.enumerate.map((y : Any) => (y, x)))
      }
    }
    "map lifts product left" in {
      forAll { (e: (Tag, Enumerable[_ <: Any]), x: Boolean, i: BigInt) =>
        Enumeration.singleton(x).product(e._2.enumerate) should
          equalOrExceptAtIndex[(Any, Any)](i)(e._2.enumerate.map((y : Any) => (x, y)))
      }
    }
    "map lifts sum right" in {
      forAll { (e: (Tag, Enumerable[_ <: Any]), i: BigInt) =>
        Enumeration.empty.union(e._2.enumerate.map(Right(_))) should
          equalOrExceptAtIndex[Any](i)(e._2.enumerate.map(Right(_)))
      }
    }
    "map lifts sum left" in {
      forAll { (e: (Tag, Enumerable[_ <: Any]), i: BigInt) =>
        e._2.enumerate.map(Left(_)).union(Enumeration.empty) should
          equalOrExceptAtIndex(i)(e._2.enumerate.map(Left(_)))
      }
    }
  }
  
}