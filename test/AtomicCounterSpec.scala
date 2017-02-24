import domain.services.AtomicCounter
import org.scalatestplus.play._

/** A very simple unit-test example. */
class AtomicCounterSpec extends PlaySpec {

  "AtomicCounter" should {

    "produce increasing values" in {
      val counter: AtomicCounter = new AtomicCounter
      counter.nextCount() mustBe 0
      counter.nextCount() mustBe 1
      counter.nextCount() mustBe 2
    }
  }
}
