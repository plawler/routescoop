package models

import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json.Json

class UserSpec extends FlatSpec with Matchers {

  "A user" should "be read from json" in new Fixture {
    val json = Json.parse(rawUserJson)
    val user = json.as[Profile]
    user.userId shouldEqual "582efc4a761e61454c13410d"
    user.email shouldEqual "paul.lawler+routescooptest1@gmail.com"
    user.name shouldEqual "paul.lawler+routescooptest1"
  }

  trait Fixture {
    val rawUserJson =
      """
        |{
        |   "email_verified":true,
        |   "email":"paul.lawler+routescooptest1@gmail.com",
        |   "clientID":"sfyzEivOxSFgoVD95oVVfq49w7hKTCJ7",
        |   "updated_at":"2016-11-29T22:28:36.392Z",
        |   "name":"paul.lawler+routescooptest1@gmail.com",
        |   "picture":"https://s.gravatar.com/avatar/d8dc4888794e249ba39d0510d76a8b01?s=480&r=pg&d=https%3A%2F%2Fcdn.auth0.com%2Favatars%2Fpa.png",
        |   "user_id":"auth0|582efc4a761e61454c13410d",
        |   "nickname":"paul.lawler+routescooptest1",
        |   "identities":[
        |      {
        |         "user_id":"582efc4a761e61454c13410d",
        |         "provider":"auth0",
        |         "connection":"Username-Password-Authentication",
        |         "isSocial":false
        |      }
        |   ],
        |   "created_at":"2016-11-18T13:04:10.208Z",
        |   "sub":"auth0|582efc4a761e61454c13410d"
        |}
      """.stripMargin
  }
}
