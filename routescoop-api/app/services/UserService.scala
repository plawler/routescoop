package services

import java.time.Instant
import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import models._
import modules.NonBlockingContext
import repositories.{StoredUserDataSync, UserDataSyncStore, UserSettingsStore, UserStore}

import scala.concurrent.{ExecutionContext, Future, blocking}


trait UserService {

  def createUser(user: User): Future[Unit]

  def getUser(userId: String): Future[Option[User]]

  def startDataSync(user: User): Future[UserDataSync]

  def completeDataSync(syncId: String, completedAt: Instant): Future[Unit]

  def createSettings(settings: UserSettings): Future[Unit]

}

@Singleton
class UserServiceImpl @Inject()(
  userStore: UserStore,
  dataSyncStore: UserDataSyncStore,
  settingsStore: UserSettingsStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends UserService {

  override def createUser(user: User): Future[Unit] = Future {
    blocking { userStore.insert(user) }
  }

  override def getUser(userId: String): Future[Option[User]] = Future {
    blocking { userStore.select(userId) }
  }

  override def startDataSync(user: User): Future[UserDataSync] = Future {
    val stored = StoredUserDataSync.create(user.id)
    dataSyncStore.insert(stored)
    val dataSync = UserDataSync(stored.id, stored.userId, stored.startedAt)
    actorSystem.eventStream.publish(StravaDataSyncStarted(dataSync))
    dataSync
  }

  override def completeDataSync(syncId: String, completedAt: Instant): Future[Unit] = Future {
    blocking { dataSyncStore.update(syncId, completedAt) }
  }

  override def createSettings(settings: UserSettings): Future[Unit] = Future {
    blocking { settingsStore.insert(settings) }
  }

}
