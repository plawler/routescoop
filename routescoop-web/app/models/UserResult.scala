package models

sealed trait UserResult
case object UserCreated extends UserResult
case class UserResultError(cause: String) extends UserResult
