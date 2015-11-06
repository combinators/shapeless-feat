package shapeless.feat
import scala.util.Try
import org.scalatest._
import org.scalatest.prop._
import org.scalatest.matchers._
import org.scalatest.prop.PropertyChecks
import org.scalacheck.Properties
import org.scalacheck.Gen
import org.scalacheck.Arbitrary
import org.scalacheck.Prop.forAll
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class AlgebraTest extends FreeSpec with GeneratorDrivenPropertyChecks with Matchers with MatcherUtil  {
  import EnumerableInstances._
  
  
  implicit val smallBigInt = Arbitrary(Gen.choose(Int.MinValue, generatorDrivenConfig.maxSize).map(BigInt(_)))
  
  
    
  "Checking algebraic properties" - {
    "index and pay" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any]), i: BigInt) =>
         e._2.enumerate should equalOrExceptAtIndex(i)(e._2.enumerate.pay)
      }
    }
    "index injective" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any])) =>
        val allElements =
          (0 to generatorDrivenConfig.maxSize) 
            .map (idx => Try(e._2.enumerate.index(idx)))
            .filter (_.isSuccess)
            .map (_.get)
        allElements.size should equal (allElements.toSet.size)
      }
    }
    "pay sum distributive" in {
      forAll { (e1: (Symbol, Enumerable[_ <: Any]), e2: (Symbol, Enumerable[_ <: Any]), i: BigInt) =>
        e1._2.enumerate.union(e2._2.enumerate).pay should 
          equalOrExceptAtIndex(i)(e1._2.enumerate.pay.union(e2._2.enumerate.pay))
      }
    }
    "pay product distributive" in {
      forAll { (e1: (Symbol, Enumerable[_ <: Any]), e2: (Symbol, Enumerable[_ <: Any]), i: BigInt) =>
        e1._2.enumerate.product(e2._2.enumerate).pay should 
          (equalOrExceptAtIndex[(Any, Any)](i)(e1._2.enumerate.pay.product(e2._2.enumerate)) and
           equalOrExceptAtIndex[(Any, Any)](i)(e1._2.enumerate.product(e2._2.enumerate.pay)))
      }
    }
    "map preserves composition" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any]), i: BigInt) =>
        e._2.enumerate.map(_.toString).map(_.hashCode) should
          equalOrExceptAtIndex(i)(e._2.enumerate.map(_.toString.hashCode))
      }
    }
    "map lifts product right" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any]), x: Boolean, i: BigInt) =>
        e._2.enumerate.product(Enumeration.singleton(x)) should
          equalOrExceptAtIndex[(Any, Any)](i)(e._2.enumerate.map((y : Any) => (y, x)))
      }
    }
    "map lifts product left" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any]), x: Boolean, i: BigInt) =>
        Enumeration.singleton(x).product(e._2.enumerate) should
          equalOrExceptAtIndex[(Any, Any)](i)(e._2.enumerate.map((y : Any) => (x, y)))
      }
    }
    "map lifts sum right" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any]), i: BigInt) =>
        Enumeration.empty.union(e._2.enumerate.map(Right(_))) should
          equalOrExceptAtIndex(i)(e._2.enumerate.map(Right(_)))
      }
    }
    "map lifts sum left" in {
      forAll { (e: (Symbol, Enumerable[_ <: Any]), i: BigInt) =>
        e._2.enumerate.map(Left(_)).union(Enumeration.empty) should
          equalOrExceptAtIndex(i)(e._2.enumerate.map(Left(_)))
      }
    }
  }
  
}