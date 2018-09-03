package repositories

import anorm._

import javax.inject.{Inject, Singleton}

import models.User
import modules.BlockingContext
import play.api.db.Database

import scala.concurrent.ExecutionContext


trait UserStore {

  val UsersTable = "users"

  def insert(user: User): Unit
  def update(user: User): Unit
  def select(id: String): Option[User]
  def destroy(): Unit

}

@Singleton
class UserSqlStore @Inject()(db: Database)(implicit @BlockingContext ec: ExecutionContext) extends UserStore {

  override def insert(user: User): Unit = db.withTransaction { implicit conn =>
    SQL"""
          INSERT INTO #$UsersTable (id, name, email, picture, stravaId, stravaToken)
          VALUES (${user.id}, ${user.name}, ${user.email}, ${user.picture}, ${user.stravaId}, ${user.stravaToken})
      """.executeInsert()
  }

  override def update(user: User): Unit = db.withTransaction { implicit conn =>
    SQL"""
        UPDATE #$UsersTable
        SET name = ${user.name},
          email = ${user.email},
          picture = ${user.picture},
          stravaId = ${user.stravaId},
          stravaToken = ${user.stravaToken}
        WHERE id = ${user.id}
      """.executeUpdate()
  }

  override def destroy(): Unit = db.withTransaction { implicit conn =>
    SQL"""
          DELETE FROM #$UsersTable
      """.execute()
  }

  override def select(id: String): Option[User] = db.withConnection { implicit conn =>
    SQL"""
          SELECT * FROM #$UsersTable WHERE id = $id
      """.as(User.parser.*).headOption
  }


}


