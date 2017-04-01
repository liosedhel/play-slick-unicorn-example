package domain.model

case class PlaceId(id: Long) extends DomainId[Long]
case class Place(id: PlaceId, name: String)
