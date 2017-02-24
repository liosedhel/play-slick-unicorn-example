package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import org.virtuslab.unicorn._
import slick.lifted.ProvenShape
import javax.inject.Singleton

import domain.model.Team
import infrastructure.repositories.utils.DbioMonadImplicits
import play.api.data.format.Formats._
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile

import scala.concurrent.ExecutionContext

@Singleton()
class StringUnicornPlay @Inject() (dbConfig: DatabaseConfig[JdbcProfile])
  extends UnicornPlay[String](dbConfig)

@Singleton()
class StringUnicornPlayJDBC @Inject() (databaseConfigProvider: DatabaseConfigProvider)
  extends StringUnicornPlay(databaseConfigProvider.get[JdbcProfile])

object StringUnicornPlayIdentifiers extends PlayIdentifiersImpl[String] {
  override val ordering: Ordering[String] = implicitly[Ordering[String]]
  override type IdCompanion[Id <: BaseId[String]] = PlayCompanion[Id]
}

case class TeamId(id: String) extends BaseId[String]
object TeamId extends StringUnicornPlayIdentifiers.IdCompanion[TeamId]

case class TeamRow(id: Option[TeamId], captain: UserId, description: String) extends WithId[String, TeamId]

trait TeamRepositoryComponent extends UserBaseRepositoryComponent {

  protected val unicornString: Unicorn[String] with HasJdbcDriver
  import unicornString._
  import unicornString.driver.api._

  class TeamsTable(tag: Tag) extends unicornString.IdTable[TeamId, TeamRow](tag, "teams"){

    def captain = column[UserId]("user_id")
    def description = column[String]("user_id")

    def captainForeignKey = foreignKey("user_fk", captain, UserTable)(_.id) //TODO also does not work, but should

    override def * : ProvenShape[TeamRow] = (id.?, captain, description) <> (TeamRow.tupled, TeamRow.unapply)
  }

  val TeamsTable = TableQuery[TeamsTable]

  class TeamsBaseIdRepository extends BaseIdRepository[TeamId, TeamRow, TeamsTable](TeamsTable)

  TeamsTable.schema.createStatements.foreach(println)
}

@Singleton
class TeamRepository @Inject()(val unicorn: LongUnicornPlayJDBC, val unicornString: StringUnicornPlayJDBC, usersRepository: UsersRepository)
  extends TeamRepositoryComponent
  with DbioMonadImplicits
   {

     val teamsBaseIdRepository = new TeamsBaseIdRepository

     def findAll()(implicit executionContext: ExecutionContext): DBIO[Seq[Team]] = {
       teamsBaseIdRepository.findAll().flatMapInner(toDomain)
    }

     def toDomain(teamRow: TeamRow)(implicit executionContext: ExecutionContext): DBIO[Team] = {
       usersRepository.findExistingByUserId(teamRow.captain).map { captain =>
          Team(teamRow.id, captain, teamRow.description)
       }
     }
}
