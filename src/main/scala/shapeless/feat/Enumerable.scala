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

import shapeless._

import VersionCompatibility._

trait Enumerable[T] extends Serializable {
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

trait EnumerableIterableInstances extends EnumerableGenericInstances {
  implicit final def enumerateIterable[T, C](implicit
    conv: C => IterableOnce[T],
    factory: Factory[T, C],
    e: Cached[Enumerable[T]]
  ): Enumerable[C] =
    new Enumerable[C] {
      lazy val enumerate: Enumeration[C] =
        Enumeration
          .singleton(factory.newBuilder.result())
          .union(e.value
            .enumerate
            .product(enumerate)
            .map { case (elem, c) => ((factory.newBuilder += elem) ++= c).result})
          .pay
    }
}


trait EnumerablePrimitiveInstances {
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
}

object Enumerable extends EnumerablePrimitiveInstances with EnumerableIterableInstances {
  def apply[T](implicit e: Enumerable[T]): Enumerable[T] = e
}