package domain.services

import javax.inject.{Inject, Singleton}

import cats.Monad
import cats.implicits._
import domain.model.GameId
import domain.services.interfaces.GamesUsersRepository

import scala.language.higherKinds

@Singleton
class StatisticsService[F[_]: Monad] @Inject()(gamesUsersRepository: GamesUsersRepository[F]) {

  def countGameParticipants(gameId: GameId): F[Long] = {
    for {
      players <- gamesUsersRepository.findPlayersByGameId(gameId)
    } yield players.size
  }

  def rootMeanSquareOfPlayersPerGame(): F[Double] = {
    for {
      gamesAndParticipants <- gamesUsersRepository.findGamesAndParticipantsNumber()
    } yield {
      val numberOfGames = gamesAndParticipants.size
      val nominator = gamesAndParticipants.map{case (_, p) => p * p}.sum
      if(numberOfGames <= 0) 0 else Math.sqrt(nominator / numberOfGames)
    }
  }
}
