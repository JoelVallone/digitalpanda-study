package calculator

sealed abstract class Expr
final case class Literal(v: Double) extends Expr
final case class Ref(name: String) extends Expr
final case class Plus(a: Expr, b: Expr) extends Expr
final case class Minus(a: Expr, b: Expr) extends Expr
final case class Times(a: Expr, b: Expr) extends Expr
final case class Divide(a: Expr, b: Expr) extends Expr

object Calculator {

  def computeValues(
      namedExpressions: Map[String, Signal[Expr]]): Map[String, Signal[Double]] =
    namedExpressions.map{
      case (name, expr) => (name, Signal{ eval(expr(), namedExpressions)})}

  def eval(expr: Expr, references: Map[String, Signal[Expr]]): Double = {
    def evalCycle(expr: Expr, references: Map[String, Signal[Expr]], touched: Set[String]): Double =
      expr match {
        case Literal(v) => v
        case Plus(a, b) => evalCycle(a, references, touched) + evalCycle(b, references, touched)
        case Minus(a, b) => evalCycle(a, references, touched) - evalCycle(b, references, touched)
        case Times(a, b) => evalCycle(a, references, touched) * evalCycle(b, references, touched)
        case Divide(a, b) => evalCycle(a, references, touched) / evalCycle(b, references, touched)
        case Ref(name) =>
          if (touched(name))
            Double.NaN
          else
            evalCycle(getReferenceExpr(name, references), references, touched + name)
        case _ => Double.NaN
      }
    evalCycle(expr, references, Set())
  }

  /** Get the Expr for a referenced variables.
   *  If the variable is not known, returns a literal NaN.
   */
  private def getReferenceExpr(name: String,
      references: Map[String, Signal[Expr]]): Expr = {
    references.get(name).fold[Expr] {
      Literal(Double.NaN)
    } { exprSignal =>
      exprSignal()
    }
  }
}