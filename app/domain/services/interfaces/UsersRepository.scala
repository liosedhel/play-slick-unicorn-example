package domain.services.interfaces

import cats.data.OptionT
import domain.model.User
import domain.model.UserId

trait UsersRepository[F[_]] {
  def findExistingByUserId(userId: UserId): F[User]
  def findByUserId(userId: UserId): OptionT[F, User]
}
