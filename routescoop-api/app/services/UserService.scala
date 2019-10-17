package services

import javax.inject.{Inject, Singleton}
import models._
import modules.NonBlockingContext
import repositories.{StravaOauthTokenStore, UserDataSyncStore, UserSettingsStore, UserStore}

import java.time.Instant
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
  tokenStore: StravaOauthTokenStore,
  publisher: Publisher
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
      userStore.select(userId) flatMap { user =>
        tokenStore.findByUserId(userId).headOption map { token =>
          user.copy(stravaToken = Some(token.accessToken))
        }
      }
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
      publisher.publish(UserSettingsCreated(settings))
    }
  }

  override def getAllSettings(userId: String) = Future {
    blocking {
      settingsStore.findByUserId(userId)
    }
  }

  override def getSettingsFor(activity: Activity): Future[Option[UserSettings]] = {
    blocking {
      Future.successful {
        settingsStore.findLatestUntil(activity.startedAt, activity.userId)
          .orElse(settingsStore.findEarliestAfter(activity.startedAt, activity.userId))
      }
    }
  }

}
