package domain.model

import infrastructure.repositories.TeamId
import play.api.libs.json.Json

case class Team(id: Option[TeamId], captain: User, description: String)

object Team {
  implicit val format = Json.format[Team]
}
