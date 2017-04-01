package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import domain.model.User
import domain.services.interfaces.GamesUsersRepository
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.UnicornPlay
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

trait GameUsersRepositoryComponent
    extends GamesBaseRepositoryComponent
    with UsersBaseRepositoryComponent {

  import unicorn._
  import unicorn.driver.api._

  class GamesUsers(tag: Tag)
      extends JunctionTable[GameId, UserId](tag, "GAMES_USERS") {

    //columns
    def gameId = column[GameId]("GAME_ID")
    def userId = column[UserId]("USER_ID")

    //constraints
    def game = foreignKey("GAME_FK", gameId, GamesTable)(_.id)

    def user = foreignKey("USER_FK", userId, UsersTable)(_.id)

    def pk = primaryKey("GAMES_USERS_PK", (gameId, userId))

    override def columns = gameId -> userId
  }

  val GamesUsersTable = TableQuery[GamesUsers]

  class GamesUsersDao
      extends JunctionRepository[GameId, UserId, GamesUsers](GamesUsersTable) {
    def findGamesAndParticipantNumber(): slick.dbio.DBIO[Seq[(GameId, Int)]] =
      GamesUsersTable
        .groupBy(_.gameId)
        .map {
          case (gameId, group) => (gameId, group.size)
        }
        .result
  }

  GamesUsersTable.schema.createStatements.foreach(println)

}

@Singleton()
class GamesUsersRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long],
                                         usersRepository: UsersRepositoryImpl)(
    implicit executionContext: ExecutionContext)
    extends GameUsersRepositoryComponent
    with GamesUsersRepository[DBIO]
    with DbioMonadImplicits {

  val gamesUsersDao = new GamesUsersDao

  def deleteByGameId(gameId: domain.model.GameId): DBIO[Int] = {
    gamesUsersDao.deleteForA(gameId)
  }

  def findPlayersByGameId(gameId: domain.model.GameId): DBIO[Seq[User]] = {
    gamesUsersDao
      .forA(gameId)
      .flatMapInner(userId => usersRepository.findExistingByUserId(userId))
  }

  def findAll(): DBIO[Seq[(domain.model.GameId, domain.model.UserId)]] = {
    gamesUsersDao.findAll().mapInner {
      case (gameId, userId) => (gameId.toDomain, userId.toDomain)
    }
  }

  def findGamesAndParticipantsNumber()
    : DBIO[Seq[(domain.model.GameId, Int)]] = {
    gamesUsersDao
      .findGamesAndParticipantNumber()
      .mapInner {
        case (gameId, participantNumber) =>
          (gameId.toDomain, participantNumber)
      }
  }
}
