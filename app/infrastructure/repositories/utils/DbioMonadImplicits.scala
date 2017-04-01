package infrastructure.repositories.utils

import cats.Monad
import play.api.libs.concurrent.Execution
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait DbioMonadImplicits extends ActionConversionImplicits {

  val monadExecutionContext = Execution.Implicits.defaultContext

  implicit val DbioMonad = new Monad[DBIO] {

    override def pure[A](x: A): DBIO[A] = DBIO.successful(x)

    override def flatMap[A, B](fa: DBIO[A])(f: (A) => DBIO[B]): DBIO[B] = {
      fa.flatMap(f)(monadExecutionContext)
    }

    override def tailRecM[A, B](a: A)(f: (A) => DBIO[Either[A, B]]): DBIO[B] = {
      f(a).flatMap {
        case Left(a1) => tailRecM(a1)(f)
        case Right(b) => DBIO.successful(b)
      }(monadExecutionContext)
    }
  }

}

trait ActionConversionImplicits {

  implicit class EnhancedSeqDbio[A](action: DBIO[Seq[A]]) {

    def mapInner[B](function: A => B)(implicit executionContext: ExecutionContext): DBIO[Seq[B]] = {
      action.map { sequence =>
        sequence.map(function)
      }
    }

    def flatMapInner[B](function: A => DBIO[B])(implicit executionContext: ExecutionContext): DBIO[Seq[B]] = {
      action.flatMap { sequence =>
        val sequenceOfActions = sequence.map(function)
        DBIO.sequence(sequenceOfActions)
      }
    }
  }

  implicit class EnhancedSetDbio[A](action: DBIO[Set[A]]) {

    def mapInner[B](function: A => B)(implicit executionContext: ExecutionContext): DBIO[Set[B]] = {
      action.map { sequence =>
        sequence.map(function)
      }
    }

    def flatMapInner[B](function: A => DBIO[B])(implicit executionContext: ExecutionContext): DBIO[Set[B]] = {
      action.flatMap { set =>
        val setOfActions = set.map(function)
        DBIO.sequence(setOfActions.toSeq).map(_.toSet)
      }
    }
  }
}
