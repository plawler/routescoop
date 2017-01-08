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
    request.cookies.get("idToken").flatMap { cookie =>
      val profileKey = cookie.value + "profile" // todo: check for validity of jwt
      cache.get[Profile](profileKey).map { profile =>
        Logger.debug("action authenticated")
        block(request)
      }
    }.getOrElse {
      Logger.debug("action unauthenticated")
      Future.successful(Redirect(routes.User.login()))
    }
  }

}
