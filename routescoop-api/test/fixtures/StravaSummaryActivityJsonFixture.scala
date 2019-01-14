package fixtures

trait StravaSummaryActivityJsonFixture {

  val stravaSummaryActivityJson =
    """
      |    {
      |        "resource_state": 2,
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "name": "Morning Ride",
      |        "distance": 0,
      |        "moving_time": 5400,
      |        "elapsed_time": 5400,
      |        "total_elevation_gain": 0,
      |        "type": "Ride",
      |        "workout_type": 12,
      |        "id": 1892419384,
      |        "external_id": "activity_3074364463.tcx",
      |        "upload_id": 2026569599,
      |        "start_date": "2018-10-08T14:55:00Z",
      |        "start_date_local": "2018-10-08T10:55:00Z",
      |        "timezone": "(GMT-05:00) America/New_York",
      |        "utc_offset": -14400,
      |        "start_latlng": null,
      |        "end_latlng": null,
      |        "location_city": null,
      |        "location_state": null,
      |        "location_country": "United States",
      |        "start_latitude": null,
      |        "start_longitude": null,
      |        "achievement_count": 0,
      |        "kudos_count": 2,
      |        "comment_count": 0,
      |        "athlete_count": 1,
      |        "photo_count": 0,
      |        "map": {
      |            "id": "a1892419384",
      |            "summary_polyline": null,
      |            "resource_state": 2
      |        },
      |        "trainer": true,
      |        "commute": false,
      |        "manual": false,
      |        "private": false,
      |        "visibility": "everyone",
      |        "flagged": false,
      |        "gear_id": "b2497339",
      |        "from_accepted_tag": false,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 84,
      |        "average_watts": 176.2,
      |        "weighted_average_watts": 196,
      |        "kilojoules": 951.5,
      |        "device_watts": true,
      |        "has_heartrate": true,
      |        "average_heartrate": 146.3,
      |        "max_heartrate": 189,
      |        "display_hide_heartrate_option": false,
      |        "max_watts": 349,
      |        "pr_count": 0,
      |        "total_photo_count": 0,
      |        "has_kudoed": false
      |    }
    """.stripMargin

  val stravaWalkJson =
    """
      |{
      |        "resource_state": 2,
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "name": "Lunch Walk",
      |        "distance": 5915.4,
      |        "moving_time": 4990,
      |        "elapsed_time": 5647,
      |        "total_elevation_gain": 167,
      |        "type": "Walk",
      |        "id": 2060551286,
      |        "external_id": "garmin_push_3278119000",
      |        "upload_id": 2201584152,
      |        "start_date": "2019-01-06T17:00:26Z",
      |        "start_date_local": "2019-01-06T12:00:26Z",
      |        "timezone": "(GMT-05:00) America/New_York",
      |        "utc_offset": -18000,
      |        "start_latlng": [
      |            34.25,
      |            -84.14
      |        ],
      |        "end_latlng": [
      |            34.25,
      |            -84.14
      |        ],
      |        "location_city": null,
      |        "location_state": null,
      |        "location_country": "United States",
      |        "start_latitude": 34.25,
      |        "start_longitude": -84.14,
      |        "achievement_count": 0,
      |        "kudos_count": 6,
      |        "comment_count": 0,
      |        "athlete_count": 1,
      |        "photo_count": 0,
      |        "map": {
      |            "id": "a2060551286",
      |            "summary_polyline": "qt`pEhfp`OnBnInDjAbF|GzHiAvB`AdBiBgCyGsBf@uAeCzD~HgFyCmCoOeFgEi@}H_AbAhAuAn@bI|EdEhCtOlFbD_DyCg@sDdA`CdC_@lC~G_CfC_KA{KuHcHaYCnAwEbBhArC_CTaBrG_CiA",
      |            "resource_state": 2
      |        },
      |        "trainer": false,
      |        "commute": false,
      |        "manual": false,
      |        "private": false,
      |        "visibility": "everyone",
      |        "flagged": false,
      |        "gear_id": null,
      |        "from_accepted_tag": false,
      |        "average_speed": 1.185,
      |        "max_speed": 2.4,
      |        "average_cadence": 51.5,
      |        "has_heartrate": true,
      |        "average_heartrate": 103.3,
      |        "max_heartrate": 133,
      |        "heartrate_opt_out": false,
      |        "display_hide_heartrate_option": false,
      |        "elev_high": 397.4,
      |        "elev_low": 263.4,
      |        "pr_count": 0,
      |        "total_photo_count": 0,
      |        "has_kudoed": false
      |    }
    """.stripMargin
}
