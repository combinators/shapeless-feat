package shapeless.feat

import scala.annotation.tailrec

sealed abstract class Finite[+A] { self =>
  val cardinality: Long
  def get(idx: Long): A
  final def map[B](f: A => B) =
    new Finite[B] {
      val cardinality = self.cardinality
      def get(idx: Long) = f (self.get(idx))
    }
  final def :+:[B >: A](left: Finite[B]): Finite[B] =
    new Finite[B] {
      val cardinality = left.cardinality + self.cardinality 
      def get(idx: Long) =
        if (idx < left.cardinality) left.get(idx) else self.get(idx - left.cardinality)
    }
  
  final def :*:[B](left: Finite[B]): Finite[(B, A)] =
    new Finite[(B, A)] {
      val cardinality = left.cardinality * self.cardinality 
      def get(idx: Long) = 
        (left.get(idx / self.cardinality), self.get(idx % self.cardinality))
    }
}

object Finite {
  final def empty[A]: Finite[A] =
    new Finite[A] {
      val cardinality = 0l
      def get(idx: Long) = throw new IndexOutOfBoundsException(idx.toString)
    }
  
  final def singleton[A](x: A): Finite[A] =
    new Finite[A] {
      val cardinality = 1l
      def get(idx: Long) =
        if (idx == 0) x else throw new IndexOutOfBoundsException(idx.toString)
    }
}

sealed abstract class Enumeration[+A] { self =>
  def parts: Stream[Finite[A]]
  final def index(idx: Long): A = {
    var i = idx
    var p = parts
    while (!p.isEmpty) {
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
  final def union[B >: A](e: => Enumeration[B]): Enumeration[B] =
    new Enumeration[B] {
      private def unfoldParts(l: => Stream[Finite[A]], r: => Stream[Finite[B]]): Stream[Finite[B]] =
        if (!l.isEmpty && !r.isEmpty) {
          (l.head :+: r.head) #:: unfoldParts(l.tail, r.tail)
        } else {
          l #::: r
        }
      lazy val parts = unfoldParts(self.parts, e.parts)
    }
  final def map[B](f: A => B): Enumeration[B] =
    new Enumeration[B] { lazy val parts = self.parts map (p => p map f) }
  
  
  
  final def reversals: Stream[Enumeration[A] with Enumeration.Reversed] = {
    def go(rev: => Stream[Finite[A]], xs: => Stream[Finite[A]]): Stream[Stream[Finite[A]]] =
      if (!xs.isEmpty) {
        lazy val revWithX = xs.head #:: rev
        revWithX #:: go(revWithX, xs.tail)
      } else {
        Stream.empty
      }
    go(Stream.empty, self.parts) map (ps => new Enumeration[A] with Enumeration.Reversed { lazy val parts = ps }) 
  }
  
  final def convolute[B](reverseYs: => Enumeration[B] with Enumeration.Reversed): Finite[(A, B)] = {
    lazy val combined = 
      (self.parts zip reverseYs.parts).foldLeft((0l, Finite.empty[(A, B)])){
        case ((card, elems), (x, y)) =>
          (card + x.cardinality * y.cardinality, elems :+: (x :*: y))
      }  
    new Finite[(A, B)] {
      lazy val cardinality = combined._1
      def get(idx: Long) =
        combined._2.get(idx)
    }
  }
  
  final private def productRev[B](reverseYss: => Stream[Enumeration[B] with Enumeration.Reversed]): Enumeration[(A, B)] ={
    def go(ry: => Enumeration[B] with Enumeration.Reversed, rys: => Stream[Enumeration[B] with Enumeration.Reversed]): Enumeration[(A, B)] =
      new Enumeration[(A, B)] {
        lazy val parts = self.convolute(ry) #:: (
            if (!rys.isEmpty) {
              go(rys.head, rys.tail).parts
            } else {
              self.parts.tail.tails.map(x => (new Enumeration[A] { lazy val parts = x }).convolute(ry)).toStream
            })
      }
    if (!self.parts.isEmpty && !reverseYss.isEmpty) {
      go(reverseYss.head, reverseYss.tail)
    } else new Enumeration[(A, B)] { lazy val parts = Stream.empty }
  }
  
  final def product[B](y: => Enumeration[B]): Enumeration[(A, B)] =
    productRev(y.reversals)
  
  final def pay: Enumeration[A] =
    new Enumeration[A] { lazy val parts = Finite.empty #:: self.parts } 
}

object Enumeration {
  sealed trait Reversed
  
  final def empty[A] = new Enumeration[A] {
    lazy val parts = Stream.empty[Finite[A]] 
  }
  
  final def singleton[A](x: A) = new Enumeration[A] {
    lazy val parts = Finite.singleton(x) #:: Stream.empty        
  }
}