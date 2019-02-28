package fixtures

import java.time.Instant

import models.StravaLap


trait LapFixture extends ActivityFixture {

  val sampleLap = StravaLap(
    "theSampleLap",
    sampleActivity.id,
    796833837,
    18629225,
    sampleActivity.athleteId,
    2,
    "Lap 1",
    1913,
    1747,
    Instant.parse("2016-10-25T19:29:03Z"),
    10793.4,
    0,
    1747,
    1,
    121.0,
    5.6,
    13.9,
    74.0,
    Some(true),
    Some(113.3),
    Some(124.6),
    Some(151.0)
  )

  val stravaLapJson =
    """
      |{
      |        "id": 7003191095,
      |        "resource_state": 2,
      |        "name": "Lap 1",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 179,
      |        "moving_time": 180,
      |        "start_date": "2019-02-15T13:56:45Z",
      |        "start_date_local": "2019-02-15T08:56:45Z",
      |        "distance": 0,
      |        "start_index": 0,
      |        "end_index": 180,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 84.4,
      |        "device_watts": true,
      |        "average_watts": 142.9,
      |        "average_heartrate": 126,
      |        "max_heartrate": 131,
      |        "lap_index": 1,
      |        "split": 1
      |    }
    """.stripMargin

  val stravaLapJsonArray =
    """
      |[
      |    {
      |        "id": 7003191095,
      |        "resource_state": 2,
      |        "name": "Lap 1",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 179,
      |        "moving_time": 180,
      |        "start_date": "2019-02-15T13:56:45Z",
      |        "start_date_local": "2019-02-15T08:56:45Z",
      |        "distance": 0,
      |        "start_index": 0,
      |        "end_index": 180,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 84.4,
      |        "device_watts": true,
      |        "average_watts": 142.9,
      |        "average_heartrate": 126,
      |        "max_heartrate": 131,
      |        "lap_index": 1,
      |        "split": 1
      |    },
      |    {
      |        "id": 7003191098,
      |        "resource_state": 2,
      |        "name": "Lap 2",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 179,
      |        "moving_time": 180,
      |        "start_date": "2019-02-15T13:59:45Z",
      |        "start_date_local": "2019-02-15T08:59:45Z",
      |        "distance": 0,
      |        "start_index": 181,
      |        "end_index": 361,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 84.5,
      |        "device_watts": true,
      |        "average_watts": 170,
      |        "average_heartrate": 137,
      |        "max_heartrate": 145,
      |        "lap_index": 2,
      |        "split": 2
      |    },
      |    {
      |        "id": 7003191099,
      |        "resource_state": 2,
      |        "name": "Lap 3",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 179,
      |        "moving_time": 180,
      |        "start_date": "2019-02-15T14:02:45Z",
      |        "start_date_local": "2019-02-15T09:02:45Z",
      |        "distance": 0,
      |        "start_index": 362,
      |        "end_index": 542,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 87.9,
      |        "device_watts": true,
      |        "average_watts": 192.3,
      |        "average_heartrate": 147.9,
      |        "max_heartrate": 155,
      |        "lap_index": 3,
      |        "split": 3
      |    },
      |    {
      |        "id": 7003191100,
      |        "resource_state": 2,
      |        "name": "Lap 4",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 179,
      |        "moving_time": 180,
      |        "start_date": "2019-02-15T14:05:45Z",
      |        "start_date_local": "2019-02-15T09:05:45Z",
      |        "distance": 0,
      |        "start_index": 543,
      |        "end_index": 723,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 85.2,
      |        "device_watts": true,
      |        "average_watts": 227.9,
      |        "average_heartrate": 156.2,
      |        "max_heartrate": 161,
      |        "lap_index": 4,
      |        "split": 4
      |    },
      |    {
      |        "id": 7003191101,
      |        "resource_state": 2,
      |        "name": "Lap 5",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 179,
      |        "moving_time": 180,
      |        "start_date": "2019-02-15T14:08:45Z",
      |        "start_date_local": "2019-02-15T09:08:45Z",
      |        "distance": 0,
      |        "start_index": 724,
      |        "end_index": 904,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 85.1,
      |        "device_watts": true,
      |        "average_watts": 253.3,
      |        "average_heartrate": 163.8,
      |        "max_heartrate": 170,
      |        "lap_index": 5,
      |        "split": 5
      |    },
      |    {
      |        "id": 7003191102,
      |        "resource_state": 2,
      |        "name": "Lap 6",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 299,
      |        "moving_time": 300,
      |        "start_date": "2019-02-15T14:11:45Z",
      |        "start_date_local": "2019-02-15T09:11:45Z",
      |        "distance": 0,
      |        "start_index": 905,
      |        "end_index": 1205,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 76.5,
      |        "device_watts": true,
      |        "average_watts": 111.8,
      |        "average_heartrate": 127.2,
      |        "max_heartrate": 171,
      |        "lap_index": 6,
      |        "split": 6
      |    },
      |    {
      |        "id": 7003191105,
      |        "resource_state": 2,
      |        "name": "Lap 7",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 1199,
      |        "moving_time": 1200,
      |        "start_date": "2019-02-15T14:16:45Z",
      |        "start_date_local": "2019-02-15T09:16:45Z",
      |        "distance": 0,
      |        "start_index": 1206,
      |        "end_index": 2406,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 82.1,
      |        "device_watts": true,
      |        "average_watts": 243.6,
      |        "average_heartrate": 162.8,
      |        "max_heartrate": 171,
      |        "lap_index": 7,
      |        "split": 7
      |    },
      |    {
      |        "id": 7003191107,
      |        "resource_state": 2,
      |        "name": "Lap 8",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 899,
      |        "moving_time": 900,
      |        "start_date": "2019-02-15T14:36:45Z",
      |        "start_date_local": "2019-02-15T09:36:45Z",
      |        "distance": 0,
      |        "start_index": 2407,
      |        "end_index": 3307,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 77.7,
      |        "device_watts": true,
      |        "average_watts": 142.2,
      |        "average_heartrate": 127.8,
      |        "max_heartrate": 165,
      |        "lap_index": 8,
      |        "split": 8
      |    },
      |    {
      |        "id": 7003191108,
      |        "resource_state": 2,
      |        "name": "Lap 9",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 1199,
      |        "moving_time": 1200,
      |        "start_date": "2019-02-15T14:51:45Z",
      |        "start_date_local": "2019-02-15T09:51:45Z",
      |        "distance": 0,
      |        "start_index": 3308,
      |        "end_index": 4508,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 83.6,
      |        "device_watts": true,
      |        "average_watts": 253.2,
      |        "average_heartrate": 159,
      |        "max_heartrate": 168,
      |        "lap_index": 9,
      |        "split": 9
      |    },
      |    {
      |        "id": 7003191109,
      |        "resource_state": 2,
      |        "name": "Lap 10",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 899,
      |        "moving_time": 900,
      |        "start_date": "2019-02-15T15:11:45Z",
      |        "start_date_local": "2019-02-15T10:11:45Z",
      |        "distance": 0,
      |        "start_index": 4509,
      |        "end_index": 5102,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 68.6,
      |        "device_watts": true,
      |        "average_watts": 119,
      |        "average_heartrate": 117.2,
      |        "max_heartrate": 160,
      |        "lap_index": 10,
      |        "split": 10
      |    },
      |    {
      |        "id": 7003191110,
      |        "resource_state": 2,
      |        "name": "Lap 11",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 1199,
      |        "moving_time": 1200,
      |        "start_date": "2019-02-15T15:26:45Z",
      |        "start_date_local": "2019-02-15T10:26:45Z",
      |        "distance": 0,
      |        "start_index": 5103,
      |        "end_index": 6303,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 83.1,
      |        "device_watts": true,
      |        "average_watts": 247.4,
      |        "average_heartrate": 157,
      |        "max_heartrate": 167,
      |        "lap_index": 11,
      |        "split": 11
      |    },
      |    {
      |        "id": 7003191112,
      |        "resource_state": 2,
      |        "name": "Lap 12",
      |        "activity": {
      |            "id": 2153647451,
      |            "resource_state": 1
      |        },
      |        "athlete": {
      |            "id": 178697,
      |            "resource_state": 1
      |        },
      |        "elapsed_time": 599,
      |        "moving_time": 600,
      |        "start_date": "2019-02-15T15:46:45Z",
      |        "start_date_local": "2019-02-15T10:46:45Z",
      |        "distance": 0,
      |        "start_index": 6304,
      |        "end_index": 6904,
      |        "total_elevation_gain": 0,
      |        "average_speed": 0,
      |        "max_speed": 0,
      |        "average_cadence": 70.5,
      |        "device_watts": true,
      |        "average_watts": 110.3,
      |        "average_heartrate": 118.9,
      |        "max_heartrate": 158,
      |        "lap_index": 12,
      |        "split": 12
      |    }
      |]
    """.stripMargin

}
