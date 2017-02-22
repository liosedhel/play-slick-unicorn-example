package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import domain.model.{Game, User}
import infrastructure.repositories.utils.DbioMonadImplicits
import org.joda.time.DateTime
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn._
import slick.dbio.DBIO
import slick.lifted.QueryBase

case class GameId(id: Long) extends AnyVal with BaseId[Long]

object GameId extends IdCompanion[GameId]

case class GameRow(id: Option[GameId], organizer: UserId) extends WithId[Long, GameId]

trait GameBaseRepositoryComponent extends UserBaseRepositoryComponent {
  self: UnicornWrapper[Long] =>

  import unicorn._
  import unicorn.driver.api._

  class Games(tag: Tag) extends IdTable[GameId, GameRow](tag, "games"){
    def organizer = column[UserId]("organizer")

    //def date = column[DateTime]("date")

    override def *  = (id.?, organizer) <> (GameRow.tupled, GameRow.unapply)
  }

  val GamesTable = TableQuery[Games]

  val baseIdRepository = new GameBaseIdRepository

  class GameBaseIdRepository extends BaseIdRepository[GameId, GameRow, Games](GamesTable) {
    def findGameAndOrganizer(gameId: GameId): slick.dbio.DBIO[Option[(GameRow, UserRow)]] = {
      GamesTable.filter(_.id === gameId).join(UserTable).on(_.organizer === _.id).result.headOption
    }
  }

}

@Singleton
class GameRepository @Inject()(val unicorn: LongUnicornPlayJDBC)
  extends UnicornWrapper[Long]
    with GameBaseRepositoryComponent
    with UserBaseRepositoryComponent
    with DbioMonadImplicits {


  def findByGameId(gameId: GameId): OptionT[DBIO, Game] = {
    OptionT.apply(baseIdRepository.findGameAndOrganizer(gameId)).map{
      case (gameRow, userRow) => toDomain(gameRow, userRow)
    }
  }

  def deleteGame(gameId: GameId): DBIO[Int] = {
    baseIdRepository.deleteById(gameId)
  }

  def toDomain(gameRow: GameRow, userRow: UserRow): Game = {
    Game(gameRow.id, User(userRow.id, userRow.firstName), DateTime.now())
  }
}


