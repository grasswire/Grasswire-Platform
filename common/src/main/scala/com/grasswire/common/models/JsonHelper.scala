package com.grasswire.common.models

import com.grasswire.common.json_models._
import org.json4s.ext.JodaTimeSerializers
/**
 * Levi Notik
 * Date: 2/11/14
 */
object JsonHelper {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import org.json4s.native.Serialization.{ read, write }

  implicit val formats = DefaultFormats ++ JodaTimeSerializers.all + new UUIDCustomSerializer

  class UUIDCustomSerializer extends CustomSerializer[java.util.UUID](format => (
    {
      case JString(uuid) =>
        java.util.UUID.fromString(uuid)
    },
    {
      case x: java.util.UUID =>
        JString(x.toString)
    }
  ))

  def serialize[T <: AnyRef](obj: T): String = write(obj)
  def toJson[T <: AnyRef](obj: T): JValue = parse(serialize(obj))
  def parseString(s: String): JValue = parse(s)
  def deserialize[T](json: String)(implicit mf: Manifest[T]): T = read[T](json)
  def deserialize[T](json: JValue)(implicit mf: Manifest[T]): T = json.extract[T]
  def renderCompact(j: JValue) = compact(render(j))
}
