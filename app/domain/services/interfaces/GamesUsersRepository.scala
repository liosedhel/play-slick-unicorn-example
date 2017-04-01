package domain.services.interfaces

import domain.model._

import scala.language.higherKinds

trait GamesUsersRepository[F[_]] {
  def findPlayersByGameId(gameId: GameId): F[Seq[User]]
  def findAll(): F[Seq[(GameId, UserId)]]
  def findGamesAndParticipantsNumber(): F[Seq[(GameId, Int)]]
}
