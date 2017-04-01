package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import domain.model.Team
import domain.services.interfaces.UsersRepository
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn._
import play.api.data.format.Formats._
import play.api.db.slick.DatabaseConfigProvider
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape

import scala.concurrent.ExecutionContext

@Singleton()
class StringUnicornPlay @Inject()(dbConfig: DatabaseConfig[JdbcProfile])
    extends UnicornPlay[String](dbConfig)

@Singleton()
class StringUnicornPlayJDBC @Inject()(
    databaseConfigProvider: DatabaseConfigProvider)
    extends StringUnicornPlay(databaseConfigProvider.get[JdbcProfile])

object StringUnicornPlayIdentifiers extends PlayIdentifiersImpl[String] {
  override val ordering: Ordering[String] = implicitly[Ordering[String]]
  override type IdCompanion[Id <: BaseId[String]] = PlayCompanion[Id]
}

case class TeamId(id: String) extends BaseId[String]
object TeamId extends StringUnicornPlayIdentifiers.IdCompanion[TeamId]

case class TeamRow(id: Option[TeamId], captain: UserId, description: String)
    extends WithId[String, TeamId]

/**
  * Example of joining String and Long typesafe IDs
  */
trait TeamRepositoryComponent extends UsersBaseRepositoryComponent {

  protected val unicornString: Unicorn[String] with HasJdbcDriver
  import unicornString._
  import unicornString.driver.api._

  class TeamsTable(tag: Tag)
      extends unicornString.IdTable[TeamId, TeamRow](tag, "TEAMS") {

    override protected val idColumnName: String = "ID"

    def captain = column[UserId]("USER_ID")
    def description = column[String]("USER_ID")

    def captainForeignKey =
      foreignKey("USER_FK", captain, UsersTable)(_.id) //TODO also does not work, but should

    override def * : ProvenShape[TeamRow] =
      (id.?, captain, description) <> (TeamRow.tupled, TeamRow.unapply)
  }

  val TeamsTable = TableQuery[TeamsTable]

  class TeamsDao
      extends BaseIdRepository[TeamId, TeamRow, TeamsTable](TeamsTable)

  TeamsTable.schema.createStatements.foreach(println)
  implicit def toEntity(teamId: domain.model.TeamId): TeamId =
    TeamId(teamId.id)
  implicit def toDomain(teamId: TeamId): domain.model.TeamId =
    domain.model.TeamId(teamId.id)
}

@Singleton
class TeamRepository @Inject()(val unicorn: UnicornPlay[Long],
                               val unicornString: StringUnicornPlayJDBC,
                               usersRepository: UsersRepository[DBIO])
    extends TeamRepositoryComponent
    with DbioMonadImplicits {

  val teamsDao = new TeamsDao

  def findAll()(implicit executionContext: ExecutionContext): DBIO[Seq[Team]] = {
    teamsDao.findAll().flatMapInner(toDomain)
  }

  def toDomain(teamRow: TeamRow)(
      implicit executionContext: ExecutionContext): DBIO[Team] = {
    usersRepository.findExistingByUserId(teamRow.captain).map { captain =>
      Team(teamRow.id.get, captain, teamRow.description)
    }
  }
}
