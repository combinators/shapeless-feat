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
import org.scalatest.concurrent._
import org.scalatest.time.SpanSugar._
import org.scalacheck.Prop.forAll
import org.scalatest.prop.GeneratorDrivenPropertyChecks


class PerformanceTest extends FreeSpec with GeneratorDrivenPropertyChecks with Matchers with MatcherUtil with Timeouts {
  import enumerables._
  import ArbitraryInstances._
  import EnumerableInstances._
  
  "Checking performance" - {
    "Requesting index 2000" in {
      System.gc() // Try to avoid overflows in repeated sbt runs
      forAll { (e: (Symbol, Enumerable[_ <: Any])) =>
        failAfter(10 seconds) {
          e._2.enumerate should (equalOrExceptAtIndex(2000)(e._2.enumerate))
        }
        
      }
    }
  }
}