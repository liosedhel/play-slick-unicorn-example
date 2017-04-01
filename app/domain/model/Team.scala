package domain.model

case class TeamId(id: String) extends DomainId[String]
case class Team(id: TeamId, captain: User, description: String)
