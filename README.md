# shapeless-feat
[![Maven Central](https://img.shields.io/maven-central/v/org.combinators/shapeless-feat_2.13.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.combinators%22%20AND%20%22shapeless-feat%22)
[![build status](https://github.com/combinators/shapeless-feat/workflows/Test%20code,%20update%20coverage,%20and%20release%20master%20branch/badge.svg?branch=master)](https://github.com/combinators/shapeless-feat/actions?query=workflow%3A%22Test+code%2C+update+coverage%2C+and+release+master+branch%22)
[![Coverage Status](https://coveralls.io/repos/github/combinators/shapeless-feat/badge.svg?branch=master)](https://coveralls.io/github/combinators/shapeless-feat?branch=master)
[![Join the chat at https://gitter.im/combinators/shapeless-feat](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/combinators/shapeless-feat)
## Shapeless Generic Functional Enumeration of Algebraic Data Types for Scala

This project brings [testing-feat](http://hackage.haskell.org/package/testing-feat) from Haskell over to Scala. The generics approach used in feat fits well with [shapeless](https://github.com/milessabin/shapeless), hence the name and out of the box support for generic programming.

You may find detailed information about feat in the paper by Duregård et al.:

Duregård, Jonas, Patrik Jansson, and Meng Wang. "Feat: functional enumeration of algebraic types." ACM SIGPLAN Notices 47.12 (2013): 61-72. Online [here](https://kar.kent.ac.uk/47486/1/enumeration-algebraic-types_Feat.pdf) and [here](http://dl.acm.org/citation.cfm?id=2364515).

## Installation
The current release is available at maven central, just add 
```scala
libraryDependencies += "org.combinators" %% "shapeless-feat" % "VERSIONNUMBER"
```
Currently, Scala 2.11, 2.12, and 2.13 are supported in the released version.

## Examples
Can be found in the [examples project](https://github.com/combinators/shapeless-feat/tree/master/examples/src/main/scala) and the [tests](https://github.com/combinators/shapeless-feat/tree/master/src/test/scala/shapeless/feat).

## Help and Contributions

Try the Gitter channel of [cls-scala](https://gitter.im/combinators/cls-scala).

### Contributers
- Jan Bessai

### Your name here
Just the usual: open pull requests and or issues. Feel free to add yourself to the list in this file, if you contribute something.
