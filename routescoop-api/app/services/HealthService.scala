package services

import anorm._
import javax.inject.{Inject, Singleton}
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext

@Singleton
class HealthService @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext) {

  def isDatabaseHealthy: Boolean = db.withConnection { implicit conn =>
    SQL"""
          SELECT 1
      """.execute()
  }

  def isStravaHealthy: Boolean = ???

}
