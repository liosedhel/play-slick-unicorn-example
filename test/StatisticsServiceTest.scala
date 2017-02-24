
import org.scalatest._
import domain.model.services.StatisticsService
import cats.Id
import domain.services.StatisticsService
import domain.services.interfaces.GamesUsersRepository
import infrastructure.repositories.{GameId, UserId}
import org.scalamock.scalatest.MockFactory

class StatisticsServiceTest extends FlatSpec with Matchers with GivenWhenThen with MockFactory{

  "Statistics service" should
    "compute proper average number players per game" in new Fixture {
      Given("statistic service with some repository mock")
      val gamesAndUsers = Seq((GameId(1), UserId(1)),
        (GameId(2), UserId(1)),
        (GameId(2), UserId(2)),
        (GameId(2), UserId(3))
      )
      (gamesUsersRepositoryMock.findAll _).expects().returning(gamesAndUsers)

      When("calculating the average")
      val averageNumberOfPlayersPerGame = statisticService.averageNumberOfPlayersPerGame()

      Then("average must be calculated properly")
      averageNumberOfPlayersPerGame shouldBe 2
    }

  trait Fixture {

    val gamesUsersRepositoryMock = mock[GamesUsersRepository[Id]]
    val statisticService = new StatisticsService(gamesUsersRepositoryMock)

  }

}
