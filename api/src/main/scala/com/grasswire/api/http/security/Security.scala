package com.grasswire.api.http.security

import java.nio.charset.Charset
import java.util.UUID
import javax.crypto

import org.joda.time.{ DateTime, DateTimeZone }
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory
import spray.http.{ HttpChallenge, HttpHeaders }
import spray.routing.RequestContext

import scalaz.\/

/**
 * Levi Notik
 * Date: 1/12/14
 */
object Security {

  private lazy val secLogger = LoggerFactory.getLogger("com.grasswire.management.http.security.Security")

  lazy val adminRealm = "admin realm"
  lazy val userRealm = "user realm"

  object Headers {
    lazy val UserAuthenticateUserRealm = HttpHeaders.`WWW-Authenticate`(HttpChallenge.apply("Basic", userRealm) :: Nil)
  }

  def getCredentials(r: RequestContext): GWAuthCredentials = {
    val headers = r.request.headers
    GWAuthCredentials(headers.filter(header => header.is("username")).head.value, headers.filter(header => header.is("timestamp")).head.value.toLong, headers.filter(header => header.is("uuid")).head.value, headers.filter(header => header.is("digest")).head.value)
  }

  def generateSessionKey = UUID.randomUUID().toString

  def credentialsAreValid(token: String, suppliedCredentials: GWAuthCredentials): Boolean = {
    val serverTimeUtc = DateTime.now().withZone(DateTimeZone.UTC).getMillis
    val timestampIsValid: Boolean = (serverTimeUtc / 1000) - suppliedCredentials.timestamp < 240
    timestampIsValid && {
      val SHA1 = "HmacSHA1"
      val mac = crypto.Mac.getInstance(SHA1)
      val key = new crypto.spec.SecretKeySpec(bytes(token), SHA1)
      mac.init(key)
      val baseString = suppliedCredentials.timestamp.toString :: suppliedCredentials.uuid :: Nil mkString "_"
      val hash = mac.doFinal(bytes(baseString))
      val hexDecodedHash = HexString.unapply(suppliedCredentials.digest)
      hexDecodedHash match {
        case None => false
        case Some(decoded) => java.util.Arrays.equals(hash, decoded)
      }
    }
  }

  private def bytes(str: String) = str.getBytes(Charset.forName("UTF-8"))

  object HexString {
    def unapply(s: String): Option[Array[Byte]] =
      """^([0-9a-f]{2})+$""".r.findFirstIn(s).map(hex2Bytes)
  }

  def hex2Bytes(hex: String): Array[Byte] = {
    (for { i <- 0 to hex.length - 1 by 2 if i > 0 || !hex.startsWith("0x") }
      yield hex.substring(i, i + 2))
      .map(Integer.parseInt(_, 16).toByte).toArray
  }

  case class GWAuthCredentials(username: String, timestamp: Long, uuid: String, digest: String)

  case class PermissionService(validate: (Option[String], GWAuthCredentials) => String \/ String) {

    def validateCredentials(sharedSecret: Option[String], credentials: GWAuthCredentials) =
      validate(sharedSecret, credentials)

  }
}
