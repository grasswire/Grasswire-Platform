package utils

import com.twitter.Autolink

/**
 * Created by levinotik on 1/12/15.
 */
object Parsers {

  val autolink: Autolink = new Autolink()
  autolink.setUrlTarget("_blank")

  def autoParseLinks(text: String): String = {

     autolink.autoLinkURLs(text);
  }
}
