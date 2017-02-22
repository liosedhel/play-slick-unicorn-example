package domain.model

import infrastructure.repositories.PlaceId
import play.api.libs.json.Json

case class Place(id: Option[PlaceId], name: String)

object Place {
  implicit val format = Json.format[Place]
}
