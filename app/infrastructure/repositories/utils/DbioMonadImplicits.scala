package infrastructure.repositories.utils

import cats.Monad
import play.api.libs.concurrent.Execution
import slick.dbio.DBIO

trait DbioMonadImplicits {

  val monadExecutionContext = Execution.Implicits.defaultContext

  implicit val DbioMonad = new Monad[DBIO] {

    override def pure[A](x: A): DBIO[A] = DBIO.successful(x)

    override def flatMap[A, B](fa: DBIO[A])(f: (A) => DBIO[B]): DBIO[B] = {
      fa.flatMap(f)(monadExecutionContext)
    }

    override def tailRecM[A, B](a: A)(f: (A) => DBIO[Either[A, B]]): DBIO[B] = {
      f(a).flatMap{
        case Left(a1) => tailRecM(a1)(f)
        case Right(b) => DBIO.successful(b)
      }(monadExecutionContext)
    }
  }

}