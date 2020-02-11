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

object VersionCompatibility {
  type LazyList[+A] = scala.collection.immutable.Stream[A]
  private[feat] val LazyList = new StreamCompanionAdapter

  type Factory[-E, +C] = scala.collection.generic.CanBuildFrom[Nothing, E, C]
  type IterableOnce[+T] = scala.collection.TraversableOnce[T]

  private[feat] class StreamCompanionAdapter {
    def from[E](it: IterableOnce[E]): scala.collection.immutable.Stream[E] =
      it.toStream
  }
  implicit def toStreamCompanion(
      adapter: StreamCompanionAdapter
  ): scala.collection.immutable.Stream.type =
    scala.collection.immutable.Stream
  implicit class NewBuilderOps[E, C](factory: Factory[E, C]) {
    def newBuilder: scala.collection.mutable.Builder[E, C] = factory.apply()
  }

}
