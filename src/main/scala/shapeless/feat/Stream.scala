/*
 * Copyright 2018-2020 Jan Bessai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shapeless.feat

import VersionCompatibility._

/** Private Stream implementation to brittle lazyness conditions imposed by standard library changes */
sealed protected[feat] trait Stream[+A] {
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

protected[feat] object Stream {
  private final case object EmptyStream extends Stream[Nothing] {
    override def isEmpty: Boolean = true
    override def head: Nothing =
      throw new java.util.NoSuchElementException("head of empty stream")
    override def tail: Stream[Nothing] = this
    override def toLazyList: LazyList[Nothing] = LazyList.empty[Nothing]
    override def append[B](other: => Stream[B]): Stream[B] = other
    override def map[B](f: Nothing => B): Stream[B] = this
    override def zip[B](other: Stream[B]): Stream[(Nothing, B)] = this
    override def tails: Stream[Stream[Nothing]] = this
  }

  private final case class Cons[A](elem: A, rest: () => Stream[A])
      extends Stream[A] {
    override def isEmpty: Boolean = false
    override val head = elem
    override lazy val tail = rest()
    override def toLazyList: LazyList[A] = elem #:: tail.toLazyList
    override def append[B >: A](other: => Stream[B]): Stream[B] =
      Cons(elem, () => tail.append(other))
    override def map[B](f: A => B): Stream[B] =
      Cons(f(elem), () => tail.map(f))
    override def zip[B](other: Stream[B]): Stream[(A, B)] = {
      if (other.isEmpty) EmptyStream
      else Cons((elem, other.head), () => tail.zip(other.tail))
    }
    override def tails: Stream[Stream[A]] =
      Cons(this, () => tail.tails)
  }

  protected[feat] def empty[A]: Stream[A] = EmptyStream
  protected[feat] def cons[A](head: A, tail: => Stream[A]): Stream[A] =
    Cons(head, () => tail)
}
