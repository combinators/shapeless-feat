package shapeless.feat

object VersionCompatibility {
  type Factory[-E, +C] = scala.collection.Factory[E, C]
}
