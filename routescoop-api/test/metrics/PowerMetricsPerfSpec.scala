package metrics

import fixtures.PowerEffortFixture
import org.scalatest.{FlatSpec, Matchers}
import PowerMetrics._
import org.saddle._

class PowerMetricsPerfSpec extends FlatSpec with Matchers {

  it should "handle large activity stream data" in new PowerEffortFixture {
    val start = System.currentTimeMillis()
    rollingAverage(data, 1)
    val end = System.currentTimeMillis()
    println(s"Rolling average execution time: ${end - start} millis")
  }

  it should "go faster with saddle rolling mean calculation" in new PowerEffortFixture {
    val v = Vec(data: _*)
    val start = System.currentTimeMillis()
    val result = v.rollingMean(1)
    val end = System.currentTimeMillis()
    println(s"Saddle rolling mean execution time: ${end - start} millis")
  }

}
