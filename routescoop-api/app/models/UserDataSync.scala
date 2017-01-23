package models

import java.time.Instant

import play.api.libs.json.{Json, OFormat}

/**
  * Created by paullawler on 1/22/17.
  */
case class UserDataSync(id: String, userId: String, startedAt: Instant)

object UserDataSync {

  implicit val dataSyncFormat: OFormat[UserDataSync] = Json.format[UserDataSync]

}

case class StravaDataSyncStarted(sync: UserDataSync)



