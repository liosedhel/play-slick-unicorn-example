package domain.model

import infrastructure.repositories.PlaceId

case class Place(id: Option[PlaceId], name: String)
