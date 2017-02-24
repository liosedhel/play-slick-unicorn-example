package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import domain.model.Place
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn._

case class PlaceId(id: Long) extends BaseId[Long]
object PlaceId extends IdCompanion[PlaceId]

case class PlaceRow(id: Option[PlaceId], name: String) extends WithId[Long, PlaceId]

trait PlaceBaseRepositoryComponent {

  protected val unicorn: Unicorn[Long] with HasJdbcDriver
  import unicorn._
  import unicorn.driver.api._

  class Places(tag: Tag) extends IdTable[PlaceId, PlaceRow](tag, "places") {
    def name = column[String]("name")

    def * = (id.?, name) <> (PlaceRow.tupled, PlaceRow.unapply)
  }

  val PlaceTable = TableQuery[Places]

  PlaceTable.schema.createStatements.foreach(println)

  class PlaceBaseIdRepository extends BaseIdRepository[PlaceId, PlaceRow, Places](PlaceTable)

}

@Singleton
class PlaceRepository @Inject()(val unicorn: LongUnicornPlayJDBC)
    extends PlaceBaseRepositoryComponent
    with DbioMonadImplicits {

  val placeBaseIdRepository = new PlaceBaseIdRepository

  def findByPlaceId(placeId: PlaceId): OptionT[slick.dbio.DBIO, Place] = {
    OptionT(placeBaseIdRepository.findById(placeId)).map(toDomain)
  }

  def toDomain(placeRow: PlaceRow): Place = {
    Place(placeRow.id, placeRow.name)
  }


}
