package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import domain.model.User
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.LongUnicornPlayJDBC
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext


trait GameUsersRepositoryComponent extends GameBaseRepositoryComponent with UserBaseRepositoryComponent {

  import unicorn._
  import unicorn.driver.api._

  class GamesUsers(tag: Tag) extends JunctionTable[GameId, UserId](tag, "games_users") {
    //columns
    def gameId = column[GameId]("game_id")
    def userId = column[UserId]("user_id")

    //constraints
    def game = foreignKey("game_fk", gameId, GamesTable)(_.id)

    def user = foreignKey("user_fk", userId, UserTable)(_.id)

    def pk = primaryKey("games_users_pk", (gameId, userId))

    override def columns = gameId -> userId
  }

  val GamesUsersTable = TableQuery[GamesUsers]

  class GamesUsersJunctionRepository extends JunctionRepository[GameId, UserId, GamesUsers](GamesUsersTable)

  GamesUsersTable.schema.createStatements.foreach(println)

}

@Singleton()
class GamesUsersRepository @Inject()(val unicorn: LongUnicornPlayJDBC, usersRepository: UsersRepository)
  extends GameUsersRepositoryComponent
    with DbioMonadImplicits {

  val gamesUsersJunctionRepository = new GamesUsersJunctionRepository

  def deleteByGameId(gameId: GameId): DBIO[Int] = {
    gamesUsersJunctionRepository.deleteForA(gameId)
  }

  def findAllUsersByGameId(gameId: GameId)(implicit executionContext: ExecutionContext): DBIO[Seq[User]] = {
    gamesUsersJunctionRepository.forA(gameId)
      .flatMapInner(user => usersRepository.findExistingByUserId(user))
  }
}
