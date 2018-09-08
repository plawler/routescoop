package fixtures

import java.util.UUID

trait SessionFixture {
  val sessionId = UUID.randomUUID().toString
  val session = "idToken" -> sessionId
}
