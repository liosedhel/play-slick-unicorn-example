package domain.model

import infrastructure.repositories.UserId
import play.api.libs.json.Json

case class User(userId: Option[UserId], firstName: String)

object User {
  implicit val format = Json.format[User]
}