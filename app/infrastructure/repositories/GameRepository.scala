package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import domain.model.Game
import infrastructure.repositories.utils.DbioMonadImplicits
import org.joda.time.DateTime
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn._
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

case class GameId(id: Long) extends AnyVal with BaseId[Long]

object GameId extends IdCompanion[GameId]

case class GameRow(id: Option[GameId], organizerId: UserId, note: String, date: DateTime, placeId: PlaceId) extends WithId[Long, GameId]

trait GameBaseRepositoryComponent extends UserBaseRepositoryComponent with PlaceBaseRepositoryComponent {
  self: UnicornWrapper[Long] =>

  import unicorn._
  import unicorn.driver.api._

  class Games(tag: Tag) extends IdTable[GameId, GameRow](tag, "games"){
    def organizerId = column[UserId]("organizer_id")

    def note = column[String]("note")

    def date = column[DateTime]("date")

    def placeId = column[PlaceId]("place_id")

    def organizer = foreignKey("organizer_fk", organizerId, UserTable)(_.id)

    def place = foreignKey("place_fk", placeId, PlaceTable)(_.id)

    override def *  = (id.?, organizerId, note, date, placeId) <> (GameRow.tupled, GameRow.unapply)
  }

  val GamesTable = TableQuery[Games]

  GamesTable.schema.createStatements.foreach(println)

  val gameBaseIdRepository = new GameBaseIdRepository

  class GameBaseIdRepository extends BaseIdRepository[GameId, GameRow, Games](GamesTable) {

    //Just and example how easily you can make join queries
    def findGameAndOrganizerAndPlace(gameId: GameId): slick.dbio.DBIO[Option[((GameRow, UserRow), PlaceRow)]] = {
      GamesTable
        .filter(_.id === gameId)
        .join(UserTable)
        .join(PlaceTable)
        .result.headOption
    }

  }

}

@Singleton
class GameRepository @Inject()(val unicorn: LongUnicornPlayJDBC,
                               usersRepository: UsersRepository,
                               placeRepository: PlaceRepository)(implicit executionContext: ExecutionContext)
  extends UnicornWrapper[Long]
    with GameBaseRepositoryComponent
    with DbioMonadImplicits {

  def findByGameId(gameId: GameId): OptionT[DBIO, Game] = {
    OptionT(gameBaseIdRepository.findById(gameId)).flatMap(toDomain)

  }

  def deleteGame(gameId: GameId): DBIO[Int] = {
    gameBaseIdRepository.deleteById(gameId)
  }

  /**
    * Translate Row to the full domain object
    */
  private def toDomain(gameRow: GameRow): OptionT[DBIO, Game] = {
    for {
      organizer <- usersRepository.findByUserId(gameRow.organizerId)
      place <- placeRepository.findByPlaceId(gameRow.placeId)
    } yield Game(gameRow.id, organizer, gameRow.note, gameRow.date, place)
  }

}


