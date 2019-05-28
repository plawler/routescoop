package metrics

import fixtures.PowerEffortFixture
import org.scalatest.{FlatSpec, Matchers}
import PowerMetrics._

class PowerMetricsPerfSpec extends FlatSpec with Matchers {

  it should "handle large activity stream data" in new PowerEffortFixture {
    val start = System.currentTimeMillis()
    rollingAverage(data, 1)
    val end = System.currentTimeMillis()
  }

}
