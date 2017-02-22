package infrastructure.repositories

import javax.inject.Singleton

import cats.data.OptionT
import domain.model.Place
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn.{BaseId, LongUnicornPlayJDBC, UnicornWrapper, WithId}
import slick.dbio.DBIO

case class PlaceId(id: Long) extends BaseId[Long]
object PlaceId extends IdCompanion[PlaceId]

case class PlaceRow(id: Option[PlaceId], name: String) extends WithId[Long, PlaceId]

trait PlaceBaseRepositoryComponent extends UnicornWrapper[Long]{

  import unicorn._
  import unicorn.driver.api._

  class Places(tag: Tag) extends IdTable[PlaceId, PlaceRow](tag, "places") {
    def name = column[String]("name")

    def * = (id.?, name) <> (PlaceRow.tupled, PlaceRow.unapply)
  }

  val places = TableQuery[Places]

  val baseIdRepository = new BaseIdRepository[PlaceId, PlaceRow, Places](places)

}

@Singleton
class PlaceRepository(val unicorn: LongUnicornPlayJDBC) extends PlaceBaseRepositoryComponent with DbioMonadImplicits {

  def findByPlaceId(placeId: PlaceId): OptionT[DBIO, Place] = {
    OptionT(baseIdRepository.findById(placeId)).map(toDomain)
  }

  def toDomain(placeRow: PlaceRow): Place = {
    Place(placeRow.id, placeRow.name)
  }


}
