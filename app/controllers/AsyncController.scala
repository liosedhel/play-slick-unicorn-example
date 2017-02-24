package controllers

import javax.inject._

import akka.actor.ActorSystem
import infrastructure.repositories.{GameId, GameRepository, TeamRepository}
import org.virtuslab.unicorn.LongUnicornPlayJDBC
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future, Promise}

/**
 * This controller creates an `Action` that demonstrates how to write
 * simple asynchronous code in a controller. It uses a timer to
 * asynchronously delay sending a response for 1 second.
 *
 * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
 * run code after a delay.
 * @param exec We need an `ExecutionContext` to execute our
 * asynchronous code.
 */
@Singleton
class AsyncController @Inject()(actorSystem: ActorSystem,
                                unicorn: LongUnicornPlayJDBC,
                                gameRepository: GameRepository,
                               teamsRepository: TeamRepository
                               )(implicit exec: ExecutionContext) extends Controller {

  /**
   * Create an Action that returns a plain text message after a delay
   * of 1 second.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/message`.
   */
  def message = Action.async {
    getFutureMessage(1.second).map { msg => Ok(msg) }
  }

  private def getFutureMessage(delayTime: FiniteDuration): Future[String] = {
    val promise: Promise[String] = Promise[String]()
    actorSystem.scheduler.scheduleOnce(delayTime) { promise.success("Hi!") }
    promise.future
  }

  def getGame(gameId: GameId) = Action.async{
    unicorn.db.run(
      gameRepository.findByGameId(gameId).value
    ).map(game => Ok(Json.toJson(game)))
  }


  import unicorn.driver.api._

  def doTransactionalOperation(gameId1: GameId, gameId2: GameId) = Action.async {
    unicorn.db.run{
        DBIO.seq(gameRepository.deleteGame(gameId1), gameRepository.deleteGame(gameId2)).transactionally
    }.map(_ => Ok)
  }

  def getTeams() = Action.async {
    unicorn.db.run{
      teamsRepository.findAll().map(teams => Ok(Json.toJson(teams)))
    }
  }

}
