package controllers

import domain.model._
import play.api.libs.json._

trait JsonFormatters {

  class DomainIdFormat[U, DId <: DomainId[U]](apply: U => DId)(implicit val format: Format[U]) extends Format[DId] {
    override def writes(o: DId): JsValue = format.writes(o.id)

    override def reads(json: JsValue): JsResult[DId] = format.reads(json).map(apply)
  }


  implicit lazy val userIdFormat = new DomainIdFormat[Long, UserId](UserId)
  implicit lazy val userFormat = Json.format[User]

  implicit lazy val placeIdFormat = new DomainIdFormat[Long, PlaceId](PlaceId)
  implicit lazy val placeFormat = Json.format[Place]

  implicit lazy val teamIdFormat = new DomainIdFormat[String, TeamId](TeamId)
  implicit lazy val teamFormat = Json.format[Team]

  implicit lazy val gameIdFormat = new DomainIdFormat[Long, GameId](GameId)
  implicit lazy val gameFormat = Json.format[Game]


}