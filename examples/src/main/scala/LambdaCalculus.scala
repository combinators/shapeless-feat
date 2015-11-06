
package shapeless.feat.examples

package lambda {
  
  sealed trait LambdaExpr
  case class Var(name: String) extends LambdaExpr
  case class Lam(variable: Var, body: LambdaExpr) extends LambdaExpr
  case class App(left: LambdaExpr, right: LambdaExpr) extends LambdaExpr

}

object LambdaCalculus {
  import shapeless.feat.Enumerable
  import shapeless._
  
  
  val lambdaGen = Generic[lambda.LambdaExpr]
  val lambdaEnum = Enumerable[lambda.LambdaExpr]
  
  // The lambda expression number 12345
  val expr12345 = lambdaEnum.enumerate.index(12345)
  // Lambda expressions and the number of possibilities for expressions of the same size
  val allExpressions = lambdaEnum.enumerate.values
  // A lambda expression of size 100
  val someExpr = lambdaEnum.enumerate.values(100)._2(12345)
  // The number of lambda expressions of size 100
  val sizeCount = lambdaEnum.enumerate.values(100)._1
}