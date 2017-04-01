package domain.model

import org.joda.time.DateTime

case class GameId(id: Long) extends DomainId[Long]
case class Game(id: GameId, organizer: User, note: String, date: DateTime, place: Place)
