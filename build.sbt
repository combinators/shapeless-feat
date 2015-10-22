name := "shapeless-feat"
version := "0.1"
resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)
scalaVersion := "2.11.7"
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-language:implicitConversions"
)
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.0-SNAPSHOT"

