# shapeless-feat
## Shapeless Generic Functional Enumeration of Algebraic Data Types for Scala

This project brings [testing-feat](http://hackage.haskell.org/package/testing-feat) from Haskell over to Scala. The generics approach used in feat fits well with [shapeless](https://github.com/milessabin/shapeless), hence the name and out of the box support for generic programming.

You may find detailed information about feat in the paper by Duregård et al.:

Duregård, Jonas, Patrik Jansson, and Meng Wang. "Feat: functional enumeration of algebraic types." ACM SIGPLAN Notices 47.12 (2013): 61-72. Online [here](https://kar.kent.ac.uk/47486/1/enumeration-algebraic-types_Feat.pdf) and [here](http://dl.acm.org/citation.cfm?id=2364515).

## Installation
To obtain the latest version, clone the reopsitory and run sbt and publishLocal.

The current release is available at maven central, just add 
```scala
libraryDependencies += "de.tu_dortmund.cs.ls14" %% "shapeless-feat" % "0.2.1"
```
Currently, Scala 2.11 and 2.12 are supported in the released version.

## Examples
Can be found in the [examples project](https://github.com/JanBessai/shapeless-feat/tree/master/examples/src/main/scala) and the [tests](https://github.com/JanBessai/shapeless-feat/tree/master/core/src/test/scala/shapeless/feat).

## Help and Contributions

There is no Gitter/IRC channel yet, but you might try [shapeless](https://gitter.im/milessabin/shapeless) and find a contributor there.

### Contributers
- Jan Bessai

### Your name here
Just the usual: open pull requests and or issues. Feel free to add yourself to the list in this file, if you contribute something.
