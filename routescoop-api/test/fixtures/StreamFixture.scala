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
    Some(34.076626),
    Some(-84.27327),
    Some(0.0),
    Some(227.4),
    Some(21),
    Some(0.0),
    Some(0.0),
    Some(87),
    Some(77),
    Some(193),
    Some(false)
  )



}
