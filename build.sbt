import sbt.{Credentials, Developer, ScmInfo}

lazy val commonSettings = Seq(
  organization := "org.combinators",
  scalaVersion := "2.13.3",
  crossScalaVersions := Seq("2.11.12", "2.12.12", scalaVersion.value),
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),
  headerLicense := Some(HeaderLicense.ALv2("2018-2020", "Jan Bessai")),
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
  ),
  javaOptions in Test := Seq("-Xss16m"),
  fork in Test := true,
  scapegoatVersion in ThisBuild := "1.4.6"
) ++ publishSettings

lazy val root = Project(id = "shapeless-feat", base = file("."))
  .settings(commonSettings: _*)
  .settings(
    moduleName := "shapeless-feat",
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.3",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % "test",
    libraryDependencies += "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0" % "test",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.14.1" % "test",
    unmanagedSourceDirectories in Compile += {
      val sourceDir = (sourceDirectory in Compile).value
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, n)) if n >= 13 => sourceDir / "scala-2.13+"
        case _                       => sourceDir / "scala-2.12-"
      }
    }
  )

lazy val examples =
  Project(id = "shapeless-feat-examples", base = file("examples"))
    .settings(commonSettings: _*)
    .settings(noPublishSettings: _*)
    .dependsOn(root)
    .settings(
      moduleName := "shapeless-feat-examples"
    )

lazy val publishSettings = Seq(
  homepage := Some(url("https://www.github.com/combinators/shapeless-feat")),
  licenses := Seq(
    "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt")
  ),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/combinators/shapeless-feat"),
      "scm:git:git@github.com:combinators/shapeless-feat.git"
    )
  ),
  developers := List(
    Developer(
      "JanBessai",
      "Jan Bessai",
      "jan.bessai@tu-dortmund.de",
      url("http://janbessai.github.io")
    )
  ),
  pgpPublicRing := file("travis/local.pubring.asc"),
  pgpSecretRing := file("travis/local.secring.asc"),
  releaseEarlyWith := SonatypePublisher
)

lazy val noPublishSettings = Seq(
  publish := Seq.empty,
  publishLocal := Seq.empty,
  publishArtifact := false
)
