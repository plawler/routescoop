package controllers

import models.Profile

import play.api.cache.CacheApi
import play.api.mvc.Results._
import play.api.mvc.{ActionBuilder, Request, Result, WrappedRequest}
import javax.inject.{Inject, Singleton}

import scala.concurrent.Future


case class AuthenticatedRequest[A](profile: Profile, request: Request[A]) extends WrappedRequest[A](request)

@Singleton
class AuthenticatedAction @Inject()(cache: CacheApi) extends ActionBuilder[AuthenticatedRequest] {

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
