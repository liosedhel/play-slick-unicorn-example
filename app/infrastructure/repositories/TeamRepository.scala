package infrastructure.repositories

import javax.inject.Inject
import org.virtuslab.unicorn._
import slick.lifted.ProvenShape
import javax.inject.Singleton
import play.api.data.format.Formats._

object StringUnicornPlayIdentifiers extends PlayIdentifiersImpl[String] {
  override val ordering: Ordering[String] = implicitly[Ordering[String]]
  override type IdCompanion[Id <: BaseId[String]] = PlayCompanion[Id]
}

case class TeamId(id: String) extends BaseId[String]
object TeamId extends StringUnicornPlayIdentifiers.IdCompanion[TeamId]

case class TeamRow(id: Option[TeamId], captain: UserId, description: String) extends WithId[String, TeamId]

trait TeamRepositoryComponent
  extends UnicornWrapper[String]
    //with UserBaseRepositoryComponent //TODO this will also does not work
  {

  import unicorn.driver.api._
  import unicorn._

  class TeamsTable(tag: Tag) extends IdTable[TeamId, TeamRow](tag, "teams"){

    def captain = column[UserId]("user_id")
    def description = column[String]("user_id")

    //def captainForeignKey = foreignKey("user_fk", captain, UserTable)(_.id) //TODO also does not work, but should

    override def * : ProvenShape[TeamRow] = (id.?, captain, description) <> (TeamRow.tupled, TeamRow.unapply)
  }

  val teams = TableQuery[TeamsTable]

    teams.schema.createStatements.foreach(println)
}

@Singleton
class TeamRepository @Inject()(val unicorn: UnicornPlay[String])
  extends TeamRepositoryComponent
    //with UserBaseRepositoryComponent //TODO: this does not work, as this repository requires Unicorn[Long] not Unicorn[String]
   {


}
