package fixtures

import models.StravaStream

/**
  * Created by paullawler on 4/17/17.
  */
trait StreamFixture extends ActivityFixture {

  val sampleStream = StravaStream(
    "theSampleStream",
    sampleActivity.id,
    0,
    Some(34.076626f),
    Some(-84.27327f),
    Some(0.0f),
    Some(227.4f),
    Some(21),
    Some(0.0f),
    Some(0.0f),
    Some(87),
    Some(77),
    Some(193),
    Some(false)
  )

}
