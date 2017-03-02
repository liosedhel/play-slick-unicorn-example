package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import domain.model.User
import domain.services.interfaces.GamesUsersRepository
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.UnicornPlay
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext


trait GameUsersRepositoryComponent extends GamesBaseRepositoryComponent with UsersBaseRepositoryComponent {

  import unicorn._
  import unicorn.driver.api._

  class GamesUsers(tag: Tag) extends JunctionTable[GameId, UserId](tag, "games_users") {
    //columns
    def gameId = column[GameId]("game_id")
    def userId = column[UserId]("user_id")

    //constraints
    def game = foreignKey("game_fk", gameId, GamesTable)(_.id)

    def user = foreignKey("user_fk", userId, UsersTable)(_.id)

    def pk = primaryKey("games_users_pk", (gameId, userId))

    override def columns = gameId -> userId
  }

  val GamesUsersTable = TableQuery[GamesUsers]

  class GamesUsersDao extends JunctionRepository[GameId, UserId, GamesUsers](GamesUsersTable) {
    def findGamesAndParticipantNumber(): slick.dbio.DBIO[Seq[(GameId, Int)]] = GamesUsersTable.groupBy(_.gameId).map {
      case (gameId, group) => (gameId, group.size)
    }.result
  }

  GamesUsersTable.schema.createStatements.foreach(println)

}

@Singleton()
class GamesUsersRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long],
                                         usersRepository: UsersRepositoryImpl)
                                        (implicit executionContext: ExecutionContext)
  extends GameUsersRepositoryComponent
  with GamesUsersRepository[DBIO]
    with DbioMonadImplicits {

  val gamesUsersDao = new GamesUsersDao

  def deleteByGameId(gameId: GameId): DBIO[Int] = {
    gamesUsersDao.deleteForA(gameId)
  }

  def findPlayersByGameId(gameId: GameId): DBIO[Seq[User]] = {
    gamesUsersDao.forA(gameId)
      .flatMapInner(user => usersRepository.findExistingByUserId(user))
  }

  def findAll(): DBIO[Seq[(GameId, UserId)]] = {
    gamesUsersDao.findAll()
  }

  def findGamesAndParticipantsNumber(): DBIO[Seq[(GameId, Int)]] = {
    gamesUsersDao.findGamesAndParticipantNumber()
  }
}
