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

  def updateUser(user: User): Future[Unit]

  def getUser(userId: String): Future[Option[User]]

  def completeDataSync(syncId: String, completedAt: Instant): Future[Unit]

  def lastDataSync(user: User): Future[Option[UserDataSync]]

  def createSettings(settings: UserSettings): Future[Unit]

  def getAllSettings(userId: String): Future[Seq[UserSettings]]

  def getSettingsFor(activity: Activity): Future[Option[UserSettings]]

}

@Singleton
class UserServiceImpl @Inject()(
  userStore: UserStore,
  dataSyncStore: UserDataSyncStore,
  settingsStore: UserSettingsStore,
  actorSystem: ActorSystem
)(implicit @NonBlockingContext ec: ExecutionContext) extends UserService {

  override def createUser(user: User): Future[Unit] = Future {
    blocking {
      userStore.insert(user)
    }
  }

  override def updateUser(user: User) = Future {
    blocking {
      userStore.update(user)
    }
  }


  override def getUser(userId: String): Future[Option[User]] = Future {
    blocking {
      userStore.select(userId)
    }
  }

  // todo: move to the data sync service
  override def completeDataSync(syncId: String, completedAt: Instant): Future[Unit] = Future {
    blocking {
      dataSyncStore.update(syncId, completedAt)
    }
  }

  // todo: move to the data sync service
  override def lastDataSync(user: User): Future[Option[UserDataSync]] = Future {
    blocking{
      dataSyncStore.findByUserId(user.id).find(p => p.completedAt.nonEmpty) map { lastCompleted =>
        UserDataSync(lastCompleted.id, lastCompleted.userId, lastCompleted.startedAt)
      }
    }
  }

  override def createSettings(settings: UserSettings): Future[Unit] = Future {
    blocking {
      settingsStore.insert(settings)
    }
  }

  override def getAllSettings(userId: String) = Future {
    blocking {
      settingsStore.findByUserId(userId)
    }
  }

  override def getSettingsFor(activity: Activity): Future[Option[UserSettings]] = {
    blocking {
      settingsStore.findLatestFor(activity.userId, activity.startedAt)
    } match {
      case None => getLatest(activity.userId)
      case s @ Some(_) => Future.successful(s)
    }
  }

  private def getLatest(userId: String): Future[Option[UserSettings]] = {
    getAllSettings(userId) map { all =>
      if (all.isEmpty) None
      else all.sortWith((s1, s2) => s1.createdAt.isAfter(s2.createdAt)).headOption
    }
  }

}
