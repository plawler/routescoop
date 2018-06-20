package services

import javax.inject.Singleton
import models.Profile

import scala.concurrent.Future

@Singleton
class UserService {

  def createUser(profile: Profile): Future[Unit] = ???

}
