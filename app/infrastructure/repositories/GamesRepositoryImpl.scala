package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import domain.model
import domain.model.Game
import infrastructure.repositories.utils.DbioMonadImplicits
import org.joda.time.DateTime
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn._
import domain.services.interfaces.{GamesRepository, UsersRepository}
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

case class GameId(id: Long) extends AnyVal with BaseId[Long] {
  def toDomain = domain.model.GameId(id)
}

object GameId extends IdCompanion[GameId]

case class GameRow(id: Option[GameId], organizerId: UserId, note: String, date: DateTime, placeId: PlaceId) extends WithId[Long, GameId]

trait GamesBaseRepositoryComponent
  extends UsersBaseRepositoryComponent
  with PlacesBaseRepositoryComponent {

  import unicorn._
  import unicorn.driver.api._

  class GamesTable(tag: Tag) extends IdTable[GameId, GameRow](tag, "GAMES"){

    override protected val idColumnName: String = "ID"

    def organizerId = column[UserId]("ORGANIZER_ID")

    def note = column[String]("NOTE")

    def date = column[DateTime]("DATE")

    def placeId = column[PlaceId]("PLACE_ID")

    def organizer = foreignKey("ORGANIZER_FK", organizerId, UsersTable)(_.id)

    def place = foreignKey("PLACE_FK", placeId, PlaceTable)(_.id)

    override def *  = (id.?, organizerId, note, date, placeId) <> (GameRow.tupled, GameRow.unapply)
  }

  val GamesTable = TableQuery[GamesTable]

  GamesTable.schema.createStatements.foreach(println)

  class GamesDao extends BaseIdRepository[GameId, GameRow, GamesTable](GamesTable) {

    //Just and example how easily you can make join queries
    def findGameAndOrganizerAndPlace(gameId: GameId): slick.dbio.DBIO[Option[(GameRow, UserRow, PlaceRow)]] = {
      (for {
        game <- GamesTable if game.id === gameId
        user <- game.organizer
        place <- game.place
      } yield (game, user, place)).result.headOption
    }

  }

  implicit def toEntity(userId: domain.model.GameId): GameId = GameId(userId.id)
  implicit def toDomain(userId: GameId): domain.model.GameId = domain.model.GameId(userId.id)

}

@Singleton
class GamesRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long],
                                    usersRepository: UsersRepository[DBIO],
                                    placeRepository: PlacesRepositoryImpl)(implicit executionContext: ExecutionContext)
    extends GamesBaseRepositoryComponent
    with GamesRepository[DBIO]
    with DbioMonadImplicits {

  val gamesDao = new GamesDao

  def findByGameId(gameId: domain.model.GameId): OptionT[DBIO, Game] = {
    OptionT(gamesDao.findById(gameId)).flatMap(toDomain)

  }

  def deleteGame(gameId: domain.model.GameId): DBIO[Int] = {
    gamesDao.deleteById(gameId)
  }

  /**
    * Translate Row to the full domain object
    */
  private def toDomain(gameRow: GameRow): OptionT[DBIO, Game] = {
    for {
      organizer <- usersRepository.findByUserId(gameRow.organizerId)
      place <- placeRepository.findByPlaceId(gameRow.placeId)
    } yield Game(gameRow.id.get, organizer, gameRow.note, gameRow.date, place)
  }

}


