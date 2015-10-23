package shapeless.feat

import shapeless._
import scala.collection.generic.CanBuildFrom
import scala.collection.GenTraversableLike

trait Enumerable[T] {
  val enumerate: Enumeration[T]
}

trait EnumerableAlgebraicInstances {
  implicit final def enumerableConsProduct[H, T <: HList](implicit 
      eh: Cached[Enumerable[H]],
      et: Cached[Enumerable[T]]
  ): Enumerable[H::T] =
    new Enumerable[H::T] {
      lazy val enumerate =
          eh.value.enumerate.product(et.value.enumerate).map {
              case (h, t) => h::t 
            }
    }
  implicit val enumerableNilProduct: Enumerable[HNil] =
    new Enumerable[HNil] {
      lazy val enumerate = Enumeration.singleton(HNil) 
    }
   
  implicit final def enumerableConsCoproduct[H, T <: Coproduct](implicit
      eh: Cached[Enumerable[H]],
      et: Cached[Enumerable[T]]
  ): Enumerable[H:+:T] =
    new Enumerable[H:+:T] {
      lazy val enumerate = eh.value.enumerate.map(Inl(_)).union(et.value.enumerate.map(Inr(_)))
    }
  
  implicit val enumerableNilCoproduct: Enumerable[CNil] =
    new Enumerable[CNil] {
      lazy val enumerate = Enumeration.empty
    }
}

trait EnumerableGenericInstances extends EnumerableAlgebraicInstances {
  implicit def enumerableGeneric[T, L](implicit
      gen: Generic.Aux[T, L],
      el: Cached[Lazy[Enumerable[L]]]
  ): Enumerable[T] =
    new Enumerable[T] {
      lazy val enumerate = el.value.value.enumerate.map(gen.from).pay
    }
}

trait EnumerableDefaultInstances extends EnumerableGenericInstances {
  implicit val enumerableInt: Enumerable[Int] =
    new Enumerable[Int] {
      lazy val enumerate = Enumeration.intEnumeration
    }
  implicit val enumerableBoolean: Enumerable[Boolean] =
    new Enumerable[Boolean] {
      lazy val enumerate = Enumeration.boolEnumeration
    }
  implicit val enumerableChar: Enumerable[Char] =
    new Enumerable[Char] {
      lazy val enumerate = Enumeration.charEnumeration
    }
  
  implicit final def enumerateTraversable[T, C <% GenTraversableLike[T, C]](implicit
      cbf: CanBuildFrom[C, T, C],
      e: Cached[Enumerable[T]]
  ): Enumerable[C] =
    new Enumerable[C] {
      lazy val enumerate: Enumeration[C] =
        Enumeration
          .singleton(cbf().result())
          .union(e.value
            .enumerate
            .product(enumerate)
            .map { case (elem, c) => ((cbf() += elem) ++= c.seq).result})
          .pay
    }
}

object Enumerable extends EnumerableDefaultInstances {
  def apply[T](implicit e: Enumerable[T]): Enumerable[T] = e
}