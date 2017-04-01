package infrastructure.repositories

import javax.inject.Inject

import org.virtuslab.unicorn._
import javax.inject.Singleton

import cats.data.OptionT
import domain.model.User
import infrastructure.repositories.utils.DbioMonadImplicits
import LongUnicornPlayIdentifiers.IdCompanion
import domain.services.interfaces.UsersRepository
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

case class UserId(id: Long) extends BaseId[Long]

object UserId extends IdCompanion[UserId]

case class UserRow(id: Option[UserId], email: String, firstName: String, lastName: String) extends WithId[Long, UserId]

trait UsersBaseRepositoryComponent {

  protected val unicorn: Unicorn[Long] with HasJdbcDriver
  import unicorn._
  import unicorn.driver.api._

  class UsersTable(tag: Tag) extends IdTable[UserId, UserRow](tag, "USERS") {

    override protected val idColumnName: String = "ID"

    def email = column[String]("EMAIL")
    def firstName = column[String]("FIRST_NAME")
    def lastName = column[String]("LAST_NAME")

    override def * = (id.?, email, firstName, lastName) <> (UserRow.tupled, UserRow.unapply)
  }

  val UsersTable = TableQuery[UsersTable]

  UsersTable.schema.createStatements.foreach(println)

  class UsersDao extends BaseIdRepository[UserId, UserRow, UsersTable](UsersTable)


}

@Singleton
class UsersRepositoryImpl @Inject()(val unicorn: UnicornPlay[Long])
                                   (implicit ec: ExecutionContext)
  extends UsersBaseRepositoryComponent
  with UsersRepository[DBIO]
  with DbioMonadImplicits{

  val usersDao = new UsersDao

  def findByUserId(userId: UserId): OptionT[DBIO, User] = {
      OptionT(usersDao.findById(userId)).map(toDomain)
    }

  def findExistingByUserId(userId: UserId): DBIO[User] = {
    usersDao.findExistingById(userId).map(toDomain)
  }

  private def toDomain(userRow: UserRow): User = {
    import userRow._
    User(userRow.id, firstName)
  }
}
