package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import domain.model.Place
import domain.services.interfaces.PlaceRepository
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn._
import slick.dbio.DBIO

case class PlaceId(id: Long) extends BaseId[Long]
object PlaceId extends IdCompanion[PlaceId]

case class PlaceRow(id: Option[PlaceId], name: String)
    extends WithId[Long, PlaceId]

trait PlacesBaseRepositoryComponent {

  protected val unicorn: Unicorn[Long] with HasJdbcDriver
  import unicorn._
  import unicorn.driver.api._

  class Places(tag: Tag) extends IdTable[PlaceId, PlaceRow](tag, "PLACES") {

    override protected val idColumnName: String = "ID"

    def name = column[String]("NAME")

    def * = (id.?, name) <> (PlaceRow.tupled, PlaceRow.unapply)
  }

  val PlaceTable = TableQuery[Places]

  PlaceTable.schema.createStatements.foreach(println)

  class PlacesDao
      extends BaseIdRepository[PlaceId, PlaceRow, Places](PlaceTable)

  implicit def toEntity(userId: domain.model.PlaceId): PlaceId =
    PlaceId(userId.id)
  implicit def toDomain(userId: PlaceId): domain.model.PlaceId =
    domain.model.PlaceId(userId.id)

}

@Singleton
class PlacesRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long])
    extends PlaceRepository[DBIO]
    with PlacesBaseRepositoryComponent
    with DbioMonadImplicits {

  val placesDao = new PlacesDao

  def findByPlaceId(placeId: PlaceId): OptionT[DBIO, Place] = {
    OptionT(placesDao.findById(placeId)).map(toDomain)
  }

  def toDomain(placeRow: PlaceRow): Place = {
    Place(placeRow.id.get, placeRow.name)
  }

}
