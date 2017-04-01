package infrastructure.repositories

import javax.inject.{Inject, Singleton}

import cats.data.OptionT
import domain.model.User
import domain.services.interfaces.UsersRepository
import infrastructure.repositories.utils.DbioMonadImplicits
import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import org.virtuslab.unicorn._
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

case class UserId(id: Long) extends BaseId[Long] {
  def toDomain = domain.model.UserId(id)
}

object UserId extends IdCompanion[UserId]

case class UserRow(id: Option[UserId],
                   email: String,
                   firstName: String,
                   lastName: String)
    extends WithId[Long, UserId]

trait UsersBaseRepositoryComponent {

  protected val unicorn: Unicorn[Long] with HasJdbcDriver
  import unicorn._
  import unicorn.driver.api._

  class UsersTable(tag: Tag) extends IdTable[UserId, UserRow](tag, "USERS") {

    override protected val idColumnName: String = "ID"

    def email = column[String]("EMAIL")
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")

    override def * =
      (id.?, email, firstName, lastName) <> (UserRow.tupled, UserRow.unapply)
  }

  val UsersTable = TableQuery[UsersTable]

  UsersTable.schema.createStatements.foreach(println)

  class UsersDao
      extends BaseIdRepository[UserId, UserRow, UsersTable](UsersTable)

  implicit def toEntity(userId: domain.model.UserId): UserId =
    UserId(userId.id)
  implicit def toDomain(userId: UserId): domain.model.UserId =
    domain.model.UserId(userId.id)
}

@Singleton
class UsersRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long])(
    implicit ec: ExecutionContext)
    extends UsersBaseRepositoryComponent
    with UsersRepository[DBIO]
    with DbioMonadImplicits {

  val usersDao = new UsersDao

  def findByUserId(userId: domain.model.UserId): OptionT[DBIO, User] = {
    OptionT(usersDao.findById(userId)).map(toDomain)
  }

  def findExistingByUserId(userId: domain.model.UserId): DBIO[User] = {
    usersDao.findExistingById(userId).map(toDomain)
  }

  private def toDomain(userRow: UserRow): User = {
    import userRow._
    User(userRow.id.get, firstName)
  }
}
