package models

import org.scalatest.{FreeSpec, Matchers}
import play.api.libs.json.Json

import java.time.Instant
import scala.util.Random

class PowerEffortSpec extends FreeSpec with Matchers {

  "it should serialize to json" in {
    val p = new Random
    val hr = new Random
    val efforts = 1 to 60 map { i =>
      PowerEffort(Random.alphanumeric.take(10).mkString, i, Instant.now, hr.nextInt(200), p.nextInt(1000))
    }
    Json.prettyPrint(Json.toJson(efforts))
  }

}
