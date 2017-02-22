package infrastructure.repositories

import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.{UnicornPlay, UnicornWrapper}


trait GameUsersRepositoryComponent
  extends UnicornWrapper[Long]
  with GameBaseRepositoryComponent with UserBaseRepositoryComponent {

  import unicorn._
  import unicorn.driver.api._
 /* implicit val a = new BaseColumnType[GameId]{

  }*/
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

  GamesUsersTable.schema.createStatements.foreach(println)

  val gamesUsersJunctionRepository = new JunctionRepository[GameId, UserId, GamesUsers](GamesUsersTable)
}

class GamesUsersRepository(val unicorn: UnicornPlay[Long])
  extends GameUsersRepositoryComponent
    with DbioMonadImplicits {

  import unicorn.driver.api._

  def deleteByGameId(gameId: GameId): DBIO[Int] = {
    gamesUsersJunctionRepository.deleteForA(gameId).transactionally
  }
}
