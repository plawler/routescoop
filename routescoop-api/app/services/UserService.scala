package services

import javax.inject.{Inject, Singleton}

import models.User
import modules.BlockingContext
import repositories.UserStore

import scala.concurrent.{ExecutionContext, Future, blocking}

/**
  * Created by paullawler on 1/1/17.
  */
trait UserService {

  def createUser(user: User): Future[Unit]
  def getUser(userId: String): Future[Option[User]]

}

@Singleton
class UserServiceImpl @Inject()(userStore: UserStore)(implicit @BlockingContext ec: ExecutionContext) extends UserService {

  override def createUser(user: User): Future[Unit] = Future {
    blocking { userStore.insert(user) }
  }

  override def getUser(userId: String): Future[Option[User]] = Future {
    blocking { userStore.select(userId) }
  }

}
