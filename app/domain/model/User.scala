package domain.model

case class UserId(id: Long) extends DomainId[Long]
case class User(userId: UserId, firstName: String)
