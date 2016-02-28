package controllers

import java.util.Date

import play.api.Play
import play.api.Play.current
import scala.language.reflectiveCalls

/**
 * Controller which exposes static endpoints. In Production mode, all images, stylesheets, and javascripts are hosted
 * on Cloudfront CDN. Whe the Application launches, a timestamp is generated which forces the cache to update by
 * appending the timestamp to the asset url.
 */

object CdnAssets {

  val versionStamp: String = new Date().getTime.toString

  lazy val inDevelopmentMode: Boolean = {
    play.api.Play.isDev(play.api.Play.current)
  }

  lazy val routes: Class[_] = {
    try {
      Thread.currentThread.getContextClassLoader.loadClass("controllers.routes")
    } catch {
      case e: Throwable =>
        ClassLoader.getSystemClassLoader.loadClass("controllers.routes")
    }
  }

  lazy val CDN: Option[String] = {
    Play.configuration.getConfig("grasswire").flatMap(c => c.getString("assetscdn"))
  }

  private[this] def url(baseUrl: String) = {
    CDN.getOrElse("") + baseUrl
  }

  def at(file: String): String = {
      "/assets/" + file
  }
}
