package infrastructure.repositories

import javax.inject.Inject

import org.virtuslab.unicorn._
import javax.inject.Singleton

import org.virtuslab.unicorn.LongUnicornPlayIdentifiers.IdCompanion
import slick.dbio.DBIO

import scala.concurrent.ExecutionContext

case class UserId(id: Long) extends BaseId[Long]

object UserId extends IdCompanion[UserId]

case class UserRow(id: Option[UserId], email: String, firstName: String, lastName: String) extends WithId[Long, UserId]

trait UserBaseRepositoryComponent extends UnicornWrapper[Long] {

  import unicorn._
  import unicorn.driver.api._

  class Users(tag: Tag) extends IdTable[UserId, UserRow](tag, "users") {

    def email = column[String]("email")
    def firstName = column[String]("first_name")
    def lastName = column[String]("last_name")

    override def * = (id.?, email, firstName, lastName) <> (UserRow.tupled, UserRow.unapply)
  }

  val UserTable = TableQuery[Users]

  class UserBaseIdRepository extends BaseIdRepository[UserId, UserRow, Users](UserTable)
  val userBaseIdRepository = new UserBaseIdRepository

}

@Singleton
class UsersRepository @Inject() (val unicorn: LongUnicornPlayJDBC) extends UserBaseRepositoryComponent {

    def findExistingByUserId(userId: UserId)(implicit executionContext: ExecutionContext): DBIO[UserRow] = {
      userBaseIdRepository.findExistingById(userId)
    }
}
