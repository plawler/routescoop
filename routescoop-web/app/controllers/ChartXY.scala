package controllers

import play.api.libs.json.Json

case class ChartXY(x: Int, y: Int)

object ChartXY {
  implicit val format = Json.format[ChartXY]
}
