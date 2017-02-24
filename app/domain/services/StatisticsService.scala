package domain.services

import javax.inject.{Inject, Singleton}

import cats.Monad
import cats.implicits._
import domain.services.interfaces.GamesUsersRepository
import infrastructure.repositories.GameId

import scala.language.higherKinds

@Singleton
class StatisticsService[F[_]] @Inject()(gamesUsersRepository: GamesUsersRepository[F]) {

  def countGameParticipants(gameId: GameId)(implicit f: Monad[F]): F[Long] = {
    for {
      players <- gamesUsersRepository.findPlayersByGameId(gameId)
    } yield players.size
  }

  def averageNumberOfPlayersPerGame()(implicit f: Monad[F]): F[Double] = {
    for {
      gamesAndUsers <- gamesUsersRepository.findAll()
    } yield {
      val games = gamesAndUsers.groupBy(_._1).map(_._2.size)
      if(games.isEmpty) 0 else games.sum / games.size
    }
  }
}
