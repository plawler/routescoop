package services

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models.{StravaDataSyncStarted, User, UserDataSync}
import modules.NonBlockingContext
import repositories.{StoredUserDataSync, UserDataSyncStore, UserStore}

import scala.concurrent.{ExecutionContext, Future, blocking}


trait UserService {

  def createUser(user: User): Future[Unit]
  def getUser(userId: String): Future[Option[User]]
  def startDataSync(user: User): Future[UserDataSync] // DataSyncStarted

}

@Singleton
class UserServiceImpl @Inject()(
  userStore: UserStore,
  dataSyncStore: UserDataSyncStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends UserService {

  override def createUser(user: User): Future[Unit] = Future {
    blocking { userStore.insert(user) }
  }

  override def getUser(userId: String): Future[Option[User]] = Future {
    blocking { userStore.select(userId) }
  }

  override def startDataSync(user: User): Future[UserDataSync] = Future {
    val stored = StoredUserDataSync(UUID.randomUUID().toString, user.id, Instant.now)
    dataSyncStore.insert(stored)
    val dataSync = UserDataSync(stored.id, stored.userId, stored.startedAt)
    actorSystem.eventStream.publish(StravaDataSyncStarted(dataSync))
    dataSync
  }

}
