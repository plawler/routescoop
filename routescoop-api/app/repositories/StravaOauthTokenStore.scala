package repositories

import anorm._
import javax.inject.{Inject, Singleton}
import models.StravaOauthToken
import modules.BlockingContext

import com.typesafe.scalalogging.LazyLogging
import play.api.db.Database

import scala.concurrent.ExecutionContext


trait StravaOauthTokenStore {

  val StravaTokensTable = "strava_oauth_tokens"

  def insert(token: StravaOauthToken)
  def findByUserId(userId: String): Seq[StravaOauthToken]
  def destroy(): Unit

}

@Singleton
class StravaOauthTokenSqlStore @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext)
  extends StravaOauthTokenStore with LazyLogging {

  override def insert(token: StravaOauthToken): Unit = db.withTransaction { implicit conn =>
    SQL"""
        INSERT INTO #$StravaTokensTable (userId, accessToken, expiresAt, refreshToken)
        VALUES (
          ${token.userId},
          ${token.accessToken},
          ${token.expiresAt},
          ${token.refreshToken}
        )
      """.executeInsert()
  }

  override def findByUserId(userId: String): Seq[StravaOauthToken] = db.withConnection { implicit conn =>
    SQL"""
        SELECT *
        FROM #$StravaTokensTable
        WHERE userId = $userId
        ORDER BY expiresAt DESC
      """.as(StravaOauthToken.parser.*)
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
        DELETE FROM #$StravaTokensTable
      """.execute()
  }

}
