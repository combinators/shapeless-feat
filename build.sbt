import sbt.{Credentials, Developer, ScmInfo}

lazy val commonSettings = Seq(
  organization := "de.tu_dortmund.cs.ls14",

  scalaVersion := "2.12.4",
  crossScalaVersions := Seq("2.11.12", scalaVersion.value),

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),

  headerLicense := Some(HeaderLicense.ALv2("2018", "Jan Bessai")),

  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
   ),
   javaOptions in Test := Seq("-Xss16m"),
   fork in Test := true
 ) ++ publishSettings

lazy val root = Project(id = "shapeless-feat", base = file(".")).
  settings(commonSettings: _*).
  settings(
    moduleName := "shapeless-feat",
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.4" % "test",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.13.5" % "test"
  )

lazy val examples = Project(id = "shapeless-feat-examples", base = file("examples")).
  settings(commonSettings: _*).
  settings(noPublishSettings: _*).
  dependsOn(root).
  settings(
    moduleName := "shapeless-feat-examples"
  )


lazy val publishSettings = Seq(
  homepage := Some(url("https://www.github.com/JanBessai/shapeless-feat")),
  licenses := Seq("Apache 2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")),
  scmInfo := Some(ScmInfo(url("https://github.com/JanBessai/shapeless-feat"), "scm:git:git@github.com:JanBessai/shapeless-feat.git")),
  developers := List(
    Developer("JanBessai", "Jan Bessai", "jan.bessai@tu-dortmund.de", url("http://janbessai.github.io"))
  ),

  pgpPublicRing := file("travis/local.pubring.asc"),
  pgpSecretRing := file("travis/local.secring.asc"),
)

lazy val noPublishSettings = Seq(
  publish := Seq.empty,
  publishLocal := Seq.empty,
  publishArtifact := false
)

