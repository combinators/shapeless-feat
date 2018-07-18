package shapeless.feat

object VersionCompatibility {
  type LazyList[+A] = Stream[A]
  private[feat] val LazyList = new StreamCompanionAdapter

  type Factory[-E, +C] = scala.collection.generic.CanBuildFrom[Nothing, E, C]
  type IterableOnce[+T] = scala.collection.TraversableOnce[T]


  private[feat] class StreamCompanionAdapter {
    def from[E](it: IterableOnce[E]): Stream[E] = it.toStream
  }
  implicit def toStreamCompanion(adapter: StreamCompanionAdapter): Stream.type = Stream
  implicit class NewBuilderOps[E, C](factory: Factory[E, C]) {
    def newBuilder: scala.collection.mutable.Builder[E, C] = factory.apply()
  }

}
