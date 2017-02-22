package domain.model

import infrastructure.repositories.GameId
import org.joda.time.DateTime
import play.api.libs.json.Json


case class Game(gameId: Option[GameId], organizer: User, name: DateTime)

object Game{
  implicit val format = Json.format[Game]
}
