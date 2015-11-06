package shapeless.feat
import org.scalatest._
import org.scalatest.matchers._

trait MatcherUtil { self : Matchers =>
  def equalOrExceptAtIndex[T](i: BigInt)(y: Enumeration[T]) = new Matcher[Enumeration[T]] {
    def apply(x: Enumeration[T]) = 
        try {
          be (y.pay.index(i)) (x.index(i)) 
        } catch {
          case _: IndexOutOfBoundsException => {
            val e1 = the [IndexOutOfBoundsException] thrownBy (x.index(i))
            val e2 = the [IndexOutOfBoundsException] thrownBy (y.pay.index(i))
            be (e1.getMessage) (e2.getMessage)
        }
      }
  }
}