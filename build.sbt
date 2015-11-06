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

lazy val commonSettings = Seq(
  organization := "de.tu_dortmund.cs.ls14",
  version := "0.1",

  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots")
  ),

  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions"
  )
)

lazy val core = (Project(id = "shapeless-feat", base = file("core"))).
  settings(commonSettings: _*).
  settings(
    moduleName := "shapeless-feat",
    libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.0-SNAPSHOT",
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test",
    libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.12.5" % "test"
  )

lazy val examples = (Project(id = "shapeless-feat-examples", base = file("examples"))).
  settings(commonSettings: _*).
  dependsOn(core).
  settings(
    moduleName := "shapeless-feat-examples"
  )

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  aggregate(core, examples).
  settings(
    moduleName := "shapeless-feat-root"
  )

