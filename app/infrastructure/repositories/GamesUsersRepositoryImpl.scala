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

  class GamesUsersJunctionRepository extends JunctionRepository[GameId, UserId, GamesUsers](GamesUsersTable)

  GamesUsersTable.schema.createStatements.foreach(println)

}

@Singleton()
class GamesUsersRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long], usersRepository: UsersRepositoryImpl)(implicit executionContext: ExecutionContext)
  extends GameUsersRepositoryComponent
  with GamesUsersRepository[DBIO]
    with DbioMonadImplicits {

  val gamesUsersJunctionRepository = new GamesUsersJunctionRepository

  def deleteByGameId(gameId: GameId): DBIO[Int] = {
    gamesUsersJunctionRepository.deleteForA(gameId)
  }

  def findPlayersByGameId(gameId: GameId): DBIO[Seq[User]] = {
    gamesUsersJunctionRepository.forA(gameId)
      .flatMapInner(user => usersRepository.findExistingByUserId(user))
  }

  def findAll(): DBIO[Seq[(GameId, UserId)]] = {
    gamesUsersJunctionRepository.findAll()
  }
}
