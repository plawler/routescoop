package fixtures

trait StravaStreamJsonFixture {

  val stravaStreamJson =
    """
      |{
      |    "latlng": {
      |        "data": [
      |            [
      |                34.076626,
      |                -84.273272
      |            ],
      |            [
      |                34.07658,
      |                -84.273291
      |            ],
      |            [
      |                34.076525,
      |                -84.27332
      |            ]
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "time": {
      |        "data": [
      |            0,
      |            1,
      |            2
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "distance": {
      |        "data": [
      |            0,
      |            5.4,
      |            12.1
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "altitude": {
      |        "data": [
      |            227.4,
      |            227.4,
      |            227.4
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "heartrate": {
      |        "data": [
      |            87,
      |            87,
      |            87
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "cadence": {
      |        "data": [
      |            77,
      |            76,
      |            75
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "watts": {
      |        "data": [
      |            193,
      |            197,
      |            231
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "temp": {
      |        "data": [
      |            21,
      |            21,
      |            21
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "grade_smooth": {
      |        "data": [
      |            0,
      |            0,
      |            -1.5
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "moving": {
      |        "data": [
      |            false,
      |            true,
      |            true
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "velocity_smooth": {
      |        "data": [
      |            0,
      |            0,
      |            6
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    }
      |}
    """.stripMargin

  val stravaStreamJsonNoHeartRate =
      """
        |{
        |    "latlng": {
        |        "data": [
        |            [
        |                34.076626,
        |                -84.273272
        |            ],
        |            [
        |                34.07658,
        |                -84.273291
        |            ],
        |            [
        |                34.076525,
        |                -84.27332
        |            ]
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "time": {
        |        "data": [
        |            0,
        |            1,
        |            2
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "distance": {
        |        "data": [
        |            0,
        |            5.4,
        |            12.1
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "altitude": {
        |        "data": [
        |            227.4,
        |            227.4,
        |            227.4
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "cadence": {
        |        "data": [
        |            77,
        |            76,
        |            75
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "watts": {
        |        "data": [
        |            193,
        |            197,
        |            231
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "temp": {
        |        "data": [
        |            21,
        |            21,
        |            21
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "grade_smooth": {
        |        "data": [
        |            0,
        |            0,
        |            -1.5
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "moving": {
        |        "data": [
        |            false,
        |            true,
        |            true
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    },
        |    "velocity_smooth": {
        |        "data": [
        |            0,
        |            0,
        |            6
        |        ],
        |        "series_type": "distance",
        |        "original_size": 1748,
        |        "resolution": "high"
        |    }
        |}
      """.stripMargin

  val stravaStreamJsonNoWatts =
    """
      |{
      |    "latlng": {
      |        "data": [
      |            [
      |                34.076626,
      |                -84.273272
      |            ],
      |            [
      |                34.07658,
      |                -84.273291
      |            ],
      |            [
      |                34.076525,
      |                -84.27332
      |            ]
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "time": {
      |        "data": [
      |            0,
      |            1,
      |            2
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "distance": {
      |        "data": [
      |            0,
      |            5.4,
      |            12.1
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "altitude": {
      |        "data": [
      |            227.4,
      |            227.4,
      |            227.4
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "heartrate": {
      |        "data": [
      |            87,
      |            87,
      |            87
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "cadence": {
      |        "data": [
      |            77,
      |            76,
      |            75
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "temp": {
      |        "data": [
      |            21,
      |            21,
      |            21
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "grade_smooth": {
      |        "data": [
      |            0,
      |            0,
      |            -1.5
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "moving": {
      |        "data": [
      |            false,
      |            true,
      |            true
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "velocity_smooth": {
      |        "data": [
      |            0,
      |            0,
      |            6
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    }
      |}
    """.stripMargin

  val stravaStreamJsonNullWatts =
    """
      |{
      |    "latlng": {
      |        "data": [
      |            [
      |                34.076626,
      |                -84.273272
      |            ],
      |            [
      |                34.07658,
      |                -84.273291
      |            ],
      |            [
      |                34.076525,
      |                -84.27332
      |            ]
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "time": {
      |        "data": [
      |            0,
      |            1,
      |            2
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "distance": {
      |        "data": [
      |            0,
      |            5.4,
      |            12.1
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "altitude": {
      |        "data": [
      |            227.4,
      |            227.4,
      |            227.4
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "heartrate": {
      |        "data": [
      |            87,
      |            87,
      |            87
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "cadence": {
      |        "data": [
      |            77,
      |            76,
      |            75
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "watts": {
      |        "data": [
      |            193,
      |            197,
      |            null
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "temp": {
      |        "data": [
      |            21,
      |            21,
      |            21
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "grade_smooth": {
      |        "data": [
      |            0,
      |            0,
      |            -1.5
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "moving": {
      |        "data": [
      |            false,
      |            true,
      |            true
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    },
      |    "velocity_smooth": {
      |        "data": [
      |            0,
      |            0,
      |            6
      |        ],
      |        "series_type": "distance",
      |        "original_size": 1748,
      |        "resolution": "high"
      |    }
      |}
    """.stripMargin

}
