package utils


import com.twitter.Autolink
import com.twitter.Autolink.LinkTextModifier
import com.twitter.Extractor.Entity
import com.twitter.Extractor.Entity.Type
import com.grasswire.common.models._
import play.api.Logger
import play.api.templates.Html
import play.twirl.api.Txt

import scala.util.Try

object TwitterParser {
  type TwitterParser = Tweet => Option[play.twirl.api.Html]

  val autolink: Autolink = new Autolink()
  autolink.setUsernameIncludeSymbol(true)
  autolink.setUrlTarget("_blank")

  autolink.setLinkTextModifier(new LinkTextModifier {
    override def modify(entity: Entity, text: CharSequence): CharSequence = {

      if (entity.getType == Type.MENTION) {
        text
      } else {

        val modified = if (text.toString.length <= 30) text else text.toString.take(30) + "..."

        if (modified.toString.startsWith(" ")) modified
        else "" + modified
      }
    }
  })

  def cleanText(tweet: Tweet): String = {
    var modifiedText = tweet.text
    if (!tweet.hasYoutubeVideo) {
      tweet.entities.media.foreach { u =>
        if (u.display_url.contains("pic.twitter.com")) {
          modifiedText = modifiedText.replace(u.expanded_url, "")
        }

        if (u.url.contains("t.co")) {
          tweet.entities.media.foreach { u =>
            if (modifiedText.contains(u.url)) {
              modifiedText = modifiedText.replace(u.url, "")
            }
          }
          modifiedText = modifiedText.replace(u.expanded_url, "")
        }

      }
    }
    modifiedText
  }

  def getPicDotTwitterLinks(tweet: Tweet): String = tweet.entities.media.filter(_.display_url.contains("pic.twitter.com")).map(_.display_url).mkString("")


  def parseTweet: TwitterParser = tweet => {
    var modifiedText = tweet.text
    if (!tweet.hasYoutubeVideo) {
      tweet.entities.media.foreach { u =>
        if (u.display_url.contains("pic.twitter.com")) {
          modifiedText = modifiedText.replace(u.expanded_url, "")
        }

        if (u.url.contains("t.co")) {
          tweet.entities.media.foreach { u =>
            if (modifiedText.contains(u.url)) {
              modifiedText = modifiedText.replace(u.url, "")
            }
          }
          modifiedText = modifiedText.replace(u.expanded_url, "")
        }
      }
    }

    tweet.entities.urls.foreach(u => modifiedText = modifiedText.replace(u.url, u.expanded_url))
    try {
      val all: String = autolink.autoLink(modifiedText)
      val raw: Html = play.twirl.api.Html(all)
      Some(raw)
    } catch {
      case t: Throwable => None
    }

  }
}
