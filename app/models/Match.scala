package models

import play.api.libs.json._
import java.sql.Timestamp
import java.text.SimpleDateFormat

object Match {
  implicit val personFormat: OFormat[Match] = Json.format[Match]

  implicit object timestampFormat extends Format[Timestamp] {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'")

    def reads(json: JsValue): JsSuccess[Timestamp] = {
      val str = json.as[String]
      JsSuccess(new Timestamp(format.parse(str).getTime))
    }

    def writes(ts: Timestamp) = JsString(format.format(ts))
  }
}

case class Match(id: Long, startDate: Timestamp, endDate: Timestamp)

