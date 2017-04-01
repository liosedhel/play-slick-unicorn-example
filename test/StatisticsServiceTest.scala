
import cats.Id
import domain.services.StatisticsService
import domain.services.interfaces.GamesUsersRepository
import domain.model.GameId
import org.scalamock.scalatest.MockFactory
import org.scalatest._

class StatisticsServiceTest extends FlatSpec with Matchers with GivenWhenThen with MockFactory{

  "Statistics service" should
    "compute mean square number of players per game" in new Fixture {
      Given("statistic service with some repository mock")
      val gamesAndParticipants = Seq((GameId(1), 2), (GameId(2), 2))

      (gamesUsersRepositoryMock.findGamesAndParticipantsNumber _).expects().returning(gamesAndParticipants)

      When("calculating the root mean square")
      val averageNumberOfPlayersPerGame = statisticService.rootMeanSquareOfPlayersPerGame()

      Then("average must be calculated properly")
      averageNumberOfPlayersPerGame shouldBe 2
    }

  trait Fixture {

    val gamesUsersRepositoryMock = mock[GamesUsersRepository[Id]]
    val statisticService = new StatisticsService(gamesUsersRepositoryMock)

  }

}
