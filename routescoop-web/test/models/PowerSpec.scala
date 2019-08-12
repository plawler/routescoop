package models

import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.{Json, OWrites}

class PowerSpec extends FreeSpec with Matchers {

  "it should serialize to json" in {
    val efforts = Seq(Effort(60, 400), Effort(120, 300))
    efforts.map(effort => Map("duration" -> effort.duration.toString, "watts" -> effort.watts))
  }

}
