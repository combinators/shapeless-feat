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