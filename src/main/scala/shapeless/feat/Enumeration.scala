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

import VersionCompatibility._

sealed abstract class Finite[+A] extends Serializable { self =>
  val cardinality: BigInt
  protected [feat] def getChecked(idx: BigInt): A
  def get(idx: BigInt): A =
    if (idx >= BigInt(0) && idx < cardinality) getChecked(idx) else throw new IndexOutOfBoundsException
  final def map[B](f: A => B): Finite[B] =
    new Finite[B] {
      val cardinality = self.cardinality
      def getChecked(idx: BigInt) = f (self.getChecked(idx))
    }
  final def :+:[B >: A](left: Finite[B]): Finite[B] =
    new Finite[B] {
      val cardinality = left.cardinality + self.cardinality 
      def getChecked(idx: BigInt) =
        if (idx < left.cardinality) left.getChecked(idx) else self.getChecked(idx - left.cardinality)
    }
  
  final def :*:[B](left: Finite[B]): Finite[(B, A)] =
    new Finite[(B, A)] {
      val cardinality = left.cardinality * self.cardinality 
      def getChecked(idx: BigInt) = 
        (left.getChecked(idx / self.cardinality), self.getChecked(idx % self.cardinality))
    }
  
  final def values: LazyList[A] =
    LazyList.iterate(BigInt(0))(_ + BigInt(1)).takeWhile(_ < cardinality).map(getChecked)
}

object Finite {
  final def empty[A]: Finite[A] =
    new Finite[A] {
      val cardinality = BigInt(0)
      def getChecked(idx: BigInt) = throw new IndexOutOfBoundsException(idx.toString)
    }
  
  final def singleton[A](x: A): Finite[A] =
    new Finite[A] {
      val cardinality = BigInt(1)
      def getChecked(idx: BigInt) = x
    }
}

sealed abstract class Enumeration[+A] extends Serializable { self =>
  def parts: LazyList[Finite[A]]
  final def index(idx: BigInt): A = {
    if (idx < BigInt(0)) throw new IndexOutOfBoundsException(idx.toString)
    var i = idx
    var p = parts
    while (p.nonEmpty) {
      val h = p.head
      if (i < h.cardinality) {
        return h.get(i) 
      } else {
        i = i - h.cardinality
        p = p.tail
      }
    }
    throw new IndexOutOfBoundsException() 
  }
  
  final def values: LazyList[(BigInt, LazyList[A])] =
    parts map (p => (p.cardinality, p.values))
}

object Enumeration {
  sealed trait Reversed
  implicit class EnumerationOps[A](self: => Enumeration[A]) extends Serializable {
    final def union[B >: A](e: => Enumeration[B]): Enumeration[B] =
      new Enumeration[B] {
        private def unfoldParts(l: => LazyList[Finite[A]], r: => LazyList[Finite[B]]): LazyList[Finite[B]] =
          if (l.nonEmpty && r.nonEmpty) {
            (l.head :+: r.head) #:: unfoldParts(l.tail, r.tail)
          } else {
            l #::: r
          }
        lazy val parts = unfoldParts(self.parts, e.parts)
      }
    
    final def map[B](f: A => B): Enumeration[B] =
      new Enumeration[B] { lazy val parts = self.parts map (p => p map f) }
    
    final def reversals: LazyList[Enumeration[A] with Enumeration.Reversed] = {
      def go(rev: => LazyList[Finite[A]], xs: => LazyList[Finite[A]]): LazyList[LazyList[Finite[A]]] =
        if (xs.nonEmpty) {
          lazy val revWithX = xs.head #:: rev
          revWithX #:: go(revWithX, xs.tail)
        } else {
          LazyList.empty
        }
      go(LazyList.empty, self.parts) map (ps => new Enumeration[A] with Enumeration.Reversed { lazy val parts = ps })
    }
    
    final def convolute[B](reverseYs: => Enumeration[B] with Enumeration.Reversed): Finite[(A, B)] = {
      new Finite[(A, B)] {
        lazy val cardinality =
          self.parts.view.zip(reverseYs.parts.view).map(x => x._1.cardinality * x._2.cardinality).sum
        def getChecked(idx: BigInt): (A, B) =
          self.parts.view.zip(reverseYs.parts.view).foldLeft(Finite.empty[(A, B)]) {
            case (elems, (x, y)) => elems :+: (x :*: y)
          }.getChecked(idx)
      }
    }
    
    final private def productRev[B](reverseYss: => LazyList[Enumeration[B] with Enumeration.Reversed]): Enumeration[(A, B)] ={
      def go(ry: => Enumeration[B] with Enumeration.Reversed, rys: => LazyList[Enumeration[B] with Enumeration.Reversed]): Enumeration[(A, B)] =
        new Enumeration[(A, B)] {
          lazy val parts = self.convolute(ry) #:: (
              if (rys.nonEmpty) {
                go(rys.head, rys.tail).parts
              } else {
                LazyList.from[Finite[(A, B)]](self.parts.tail.tails.map(x => new Enumeration[A] { lazy val parts = x }.convolute(ry)))
              })
        }
      if (self.parts.nonEmpty && reverseYss.nonEmpty) {
        go(reverseYss.head, reverseYss.tail)
      } else new Enumeration[(A, B)] { lazy val parts = LazyList.empty }
    }
    
    final def product[B](y: => Enumeration[B]): Enumeration[(A, B)] =
      productRev(y.reversals)
    
    final def pay: Enumeration[A] =
      new Enumeration[A] { lazy val parts = Finite.empty #:: self.parts }
  }
  
  final def empty[A]: Enumeration[A] = new Enumeration[A] {
    lazy val parts = LazyList.empty[Finite[A]]
  }
  
  final def singleton[A](x: A): Enumeration[A] = new Enumeration[A] {
    lazy val parts = Finite.singleton(x) #:: LazyList.empty[Finite[A]]
  }
  
  final lazy val boolEnumeration: Enumeration[Boolean] =
    new Enumeration[Boolean] {
      lazy val parts = new Finite[Boolean] {
        val cardinality = BigInt(2)
        def getChecked(idx: BigInt) =
          idx.toInt match {
            case 0 => true
            case 1 => false
            case _ => throw new IndexOutOfBoundsException()
          }
        } #:: LazyList.empty[Finite[Boolean]]
    }
  
  final lazy val intEnumeration: Enumeration[Int] =
    new Enumeration[Int] {
      lazy val parts = new Finite[Int] {
          val cardinality = BigInt(Int.MaxValue) + (-1) * BigInt(Int.MinValue) + 1
          def getChecked(idx: BigInt): Int = {
            (idx % 2 + (if (idx % 2 > 0) BigInt(1) else BigInt(-1)) * (idx / 2)).toInt
          }
        } #:: LazyList.empty[Finite[Int]]
  }
  
  final lazy val charEnumeration: Enumeration[Char] =
    new Enumeration[Char] {
      lazy val parts = new Finite[Char] {
          val cardinality = BigInt(Char.MaxValue) + (-1) * BigInt(Char.MinValue) + 1
          def getChecked(idx: BigInt) = idx.toChar
        } #:: LazyList.empty[Finite[Char]]
  }
}