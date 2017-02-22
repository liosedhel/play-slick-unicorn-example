package domain.model

import infrastructure.repositories.{GameId, PlaceId, UserId}
import org.joda.time.DateTime
import play.api.libs.json.Json


case class Game(id: Option[GameId], organizer: User, note: String, date: DateTime, place: Place)

object Game{
  implicit val format = Json.format[Game]
}
