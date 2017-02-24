package domain.services.interfaces

import cats.data.OptionT
import domain.model.Game
import infrastructure.repositories.GameId

import scala.language.higherKinds


trait GamesRepository[F[_]] {

  def findByGameId(gameId: GameId): OptionT[F, Game]

  def deleteGame(gameId: GameId): F[Int]

}
