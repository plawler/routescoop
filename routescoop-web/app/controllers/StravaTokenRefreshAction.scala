package controllers

import javax.inject.{Inject, Singleton}
import models.Profile
import services.StravaOauthService

import play.api.Logger
import play.api.cache.SyncCacheApi
import play.api.mvc.Results._
import play.api.mvc.{ActionRefiner, Request, Result, WrappedRequest}

import scala.concurrent.{ExecutionContext, Future}

case class StravaRequest[A](profile: Profile, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class StravaTokenRefreshAction @Inject()(
  tokenService: StravaOauthService,
  cache: SyncCacheApi
)(implicit val executionContext: ExecutionContext) extends ActionRefiner[AuthenticatedRequest, StravaRequest] {

  // https://www.playframework.com/documentation/2.6.x/ScalaActionsComposition#Putting-it-all-together
  val tokenStatusUrl = routes.Strava.tokenStatus()

  override protected def refine[A](request: AuthenticatedRequest[A]): Future[Either[Result, StravaRequest[A]]] = {
    val profile = request.profile
    tokenService.refresh(profile) map { updatedProfile =>
      request.session.get("idToken") match {
        case Some(authToken) =>
          val key = authToken + "profile"
          cache.set(key, updatedProfile)
          Right(StravaRequest(updatedProfile, request))
        case None =>
          Logger.error(s"Oauth exchange wtih Strava failed for profile $profile. Redirecting...")
          Left(Redirect(tokenStatusUrl))
      }
    }
  }

}
