package controllers

import models.Profile

import play.api.Logger
import play.api.cache.CacheApi
import play.api.mvc.Results._
import play.api.mvc.{ActionBuilder, Request, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future


@Singleton
class AuthenticatedAction @Inject()(cache: CacheApi) extends ActionBuilder[Request] {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] = {
    request.session.get("idToken") flatMap { id =>
      val profileKey = id + "profile" // todo: check for validity of jwt
      cache.get[Profile](profileKey) map { profile =>
        block(request)
      }
    } getOrElse {
      Future.successful(Redirect(routes.Auth.login()))
    }
  }

}
