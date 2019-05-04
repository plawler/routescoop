package controllers

import javax.inject.{Inject, Singleton}
import models.Profile
import modules.NonBlockingContext

import play.api.cache.SyncCacheApi
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}


case class AuthenticatedRequest[A](profile: Profile, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class AuthenticatedAction @Inject()(
  val parser: BodyParsers.Default,
  cache: SyncCacheApi
)(implicit val executionContext: ExecutionContext) extends ActionBuilder[AuthenticatedRequest, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("idToken") map { id =>
      val profileKey = id + "profile" // todo: check for validity of jwt
      cache.get[Profile](profileKey) map { profile =>
        block(AuthenticatedRequest(profile, request))
      } getOrElse {
        Future.successful(Redirect(routes.Auth.login()))
      }
    } getOrElse {
      Future.successful(Redirect(routes.Auth.login()))
    }
  }
}
