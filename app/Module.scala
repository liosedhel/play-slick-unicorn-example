import com.google.inject.{AbstractModule, TypeLiteral}
import java.time.Clock
import javax.inject.{Inject, Singleton}

import cats.Monad
import infrastructure.repositories.{
  GamesRepositoryImpl,
  GamesUsersRepositoryImpl,
  PlacesRepositoryImpl,
  UsersRepositoryImpl
}
import org.virtuslab.unicorn.{LongUnicornPlayJDBC, UnicornPlay}
import domain.services.{ApplicationTimer, AtomicCounter, Counter}
import domain.services.interfaces.{GamesRepository, GamesUsersRepository, PlaceRepository, UsersRepository}
import play.api.libs.concurrent.Execution
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.

  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module extends AbstractModule {

  override def configure() = {
    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    bind(classOf[ApplicationTimer]).asEagerSingleton()
    // Set AtomicCounter as the implementation for Counter.
    bind(classOf[Counter]).to(classOf[AtomicCounter])
    bind(new TypeLiteral[Monad[DBIO]]()           {}).to(classOf[DbioMonadImplicits])
    bind(new TypeLiteral[PlaceRepository[DBIO]]() {})
      .to(classOf[PlacesRepositoryImpl])
    bind(new TypeLiteral[UsersRepository[DBIO]]() {})
      .to(classOf[UsersRepositoryImpl])
    bind(new TypeLiteral[GamesRepository[DBIO]]() {})
      .to(classOf[GamesRepositoryImpl])
    bind(new TypeLiteral[GamesUsersRepository[DBIO]]() {})
      .to(classOf[GamesUsersRepositoryImpl])
    bind(new TypeLiteral[UnicornPlay[Long]]() {})
      .to(classOf[LongUnicornPlayJDBC])

  }

}
@Singleton
class DbioMonadImplicits @Inject()(ec: ExecutionContext) extends Monad[DBIO] {

  override def pure[A](x: A): DBIO[A] = DBIO.successful(x)

  override def flatMap[A, B](fa: DBIO[A])(f: (A) => DBIO[B]): DBIO[B] = {
    fa.flatMap(f)(ec)
  }

  override def tailRecM[A, B](a: A)(f: (A) => DBIO[Either[A, B]]): DBIO[B] = {
    f(a).flatMap {
      case Left(a1) => tailRecM(a1)(f)
      case Right(b) => DBIO.successful(b)
    }(ec)
  }

}
