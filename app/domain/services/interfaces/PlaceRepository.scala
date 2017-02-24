package domain.services.interfaces

import cats.data.OptionT
import domain.model.Place
import infrastructure.repositories.PlaceId

import scala.language.higherKinds


trait PlaceRepository[F[_]]  {
  def findByPlaceId(placeId: PlaceId): OptionT[F, Place]
}
