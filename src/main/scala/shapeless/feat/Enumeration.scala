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

import scala.annotation.tailrec
import VersionCompatibility._

sealed protected [feat] trait Stream[+A] {
  def isEmpty: Boolean
  def nonEmpty: Boolean = !isEmpty
  def head: A
  def tail: Stream[A]
  def toLazyList: LazyList[A]
  def append[B >: A](other: => Stream[B]): Stream[B]
  def map[B](f: A => B): Stream[B]
  def zip[B](other: Stream[B]): Stream[(A, B)]
  final def foldLeft[B](start: B)(f: (B, A) => B): B = {
    var result = start
    var current = this
    while (current.nonEmpty) {
      result = f(result, current.head)
      current = current.tail
    }
    result
  }
  def tails: Stream[Stream[A]]
}

protected [feat] object Stream {
  private final case object EmptyStream extends Stream[Nothing] {
    override final def isEmpty: Boolean = true
    override final def head: Nothing = throw new java.util.NoSuchElementException("head of empty stream")
    override final def tail: Stream[Nothing] = this
    override final def toLazyList: LazyList[Nothing] = LazyList.empty[Nothing]
    override final def append[B >: Nothing](other: => Stream[B]): Stream[B] = other
    override final def map[B](f: Nothing => B): Stream[B] = this
    override final def zip[B](other: Stream[B]): Stream[(Nothing, B)] = this
    override final def tails: Stream[Stream[Nothing]] = this
  }

  private final case class Cons[A](elem: A, rest: () => Stream[A]) extends Stream[A] {
    override final def isEmpty: Boolean = false
    override final val head = elem
    override final lazy val tail = rest()
    override final def toLazyList: LazyList[A] = elem #:: tail.toLazyList
    override final def append[B >: A](other: => Stream[B]): Stream[B] =
      Cons(elem, () => tail.append(other))
    override final def map[B](f: A => B): Stream[B] =
      Cons(f(elem), () => tail.map(f))
    override final def zip[B](other: Stream[B]): Stream[(A, B)] = {
      if (other.isEmpty) EmptyStream
      else Cons((elem, other.head), () => tail.zip(other.tail))
    }
    override final def tails: Stream[Stream[A]] =
      Cons(this, () => tail.tails)
  }

  protected [feat] final def empty[A]: Stream[A] = EmptyStream
  protected [feat] final def cons[A](head: A, tail: => Stream[A]): Stream[A] = 
    Cons(head, () => tail)
}



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
  protected [feat] def pparts: Stream[Finite[A]]
  @transient final lazy val parts: LazyList[Finite[A]] = pparts.toLazyList
  final def index(idx: BigInt): A = {
    if (idx < BigInt(0)) throw new IndexOutOfBoundsException(idx.toString)
    var i = idx
    var p = pparts
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
  private final def unfoldParts[A, B >: A](l: Stream[Finite[A]], r: Stream[Finite[B]]): Stream[Finite[B]] = {
    if (l.nonEmpty && r.nonEmpty) {
      Stream.cons((l.head :+: r.head), unfoldParts(l.tail, r.tail))
    } else {
      l.append(r)
    }
  }
  private final def goReversals[A](rev: => Stream[Finite[A]], xs: Stream[Finite[A]]): Stream[Stream[Finite[A]]] = {
    if (xs.nonEmpty) {
      lazy val revWithX = Stream.cons(xs.head, rev)
      Stream.cons(revWithX, goReversals(revWithX, xs.tail))
    } else {
      Stream.empty
    }
  }

  implicit class EnumerationOps[A](self: => Enumeration[A]) extends Serializable {
    final def union[B >: A](e: => Enumeration[B]): Enumeration[B] =
      new Enumeration[B] {
        @transient final lazy val pparts = unfoldParts(self.pparts, e.pparts)
      }

    final def map[B](f: A => B): Enumeration[B] =
      new Enumeration[B] { @transient final lazy val pparts = self.pparts.map(p => p.map(f)) }

    final def reversals: Stream[Enumeration[A] with Enumeration.Reversed] = {
      goReversals(Stream.empty, self.pparts).map(ps => new Enumeration[A] with Enumeration.Reversed { @transient final lazy val pparts = ps })
    }

    final def convolute[B](reverseYs: => Enumeration[B] with Enumeration.Reversed): Finite[(A, B)] = {
      new Finite[(A, B)] {
        final lazy val cardinality =
          self.pparts.zip(reverseYs.pparts).foldLeft[BigInt](0){
            case (s, (c1, c2)) => s + c1.cardinality * c2.cardinality
          }
        final lazy private val contents: Finite[(A, B)] =
          self.parts.zip(reverseYs.parts).foldLeft(Finite.empty[(A, B)]) {
            case (elems, (x, y)) => elems :+: (x :*: y)
          }
        final def getChecked(idx: BigInt): (A, B) =
          contents.getChecked(idx)
      }
    }
    private final def goProductRev[B](ry: => Enumeration[B] with Enumeration.Reversed, rys: => Stream[Enumeration[B] with Enumeration.Reversed]): Enumeration[(A, B)] =
      new Enumeration[(A, B)] {
        @transient final lazy val pparts = 
          Stream.cons(
            self.convolute(ry),
            if (rys.nonEmpty) {
              goProductRev(rys.head, rys.tail).pparts
            } else {
              self.pparts.tail.tails.map(x => new Enumeration[A] { @transient final lazy val pparts = x }.convolute(ry))
            })
      }

    final private def productRev[B](reverseYss: => Stream[Enumeration[B] with Enumeration.Reversed]): Enumeration[(A, B)] ={
      if (self.parts.nonEmpty && reverseYss.nonEmpty) {
        goProductRev(reverseYss.head, reverseYss.tail)
      } else new Enumeration[(A, B)] { @transient final lazy val pparts = Stream.empty }
    }

    final def product[B](y: => Enumeration[B]): Enumeration[(A, B)] =
      productRev(y.reversals)

    final def pay: Enumeration[A] =
      new Enumeration[A] { @transient final lazy val pparts = Stream.cons(Finite.empty, self.pparts) }
  }

  final def empty[A]: Enumeration[A] = new Enumeration[A] {
    @transient final lazy val pparts = Stream.empty[Finite[A]]
  }

  final def singleton[A](x: A): Enumeration[A] = new Enumeration[A] {
    @transient final lazy val pparts = Stream.cons(Finite.singleton(x), Stream.empty[Finite[A]])
  }

  final lazy val boolEnumeration: Enumeration[Boolean] =
    new Enumeration[Boolean] {
      @transient final lazy val pparts = 
        Stream.cons(
          new Finite[Boolean] {
            final val cardinality = BigInt(2)
            def getChecked(idx: BigInt) =
              idx.toInt match {
                case 0 => true
                case 1 => false
                case _ => throw new IndexOutOfBoundsException()
              }
            },
          Stream.empty
        )
    }

  final lazy val intEnumeration: Enumeration[Int] =
    new Enumeration[Int] {
      @transient final lazy val pparts = 
        Stream.cons(
          new Finite[Int] {
            final val cardinality = BigInt(Int.MaxValue) + (-1) * BigInt(Int.MinValue) + 1
            def getChecked(idx: BigInt): Int = {
              (idx % 2 + (if (idx % 2 > 0) BigInt(1) else BigInt(-1)) * (idx / 2)).toInt
            }
          },
          Stream.empty
        )
  }

  final lazy val charEnumeration: Enumeration[Char] =
    new Enumeration[Char] {
      @transient final lazy val pparts = 
        Stream.cons(
          new Finite[Char] {
            final val cardinality = BigInt(Char.MaxValue) + (-1) * BigInt(Char.MinValue) + 1
            def getChecked(idx: BigInt) = idx.toChar
          },
          Stream.empty
        )
  }
}
