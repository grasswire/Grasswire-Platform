package com.grasswire.common.regex

object Regexes {

  val twitterStatusUrlRegex = """^(https?://)?(?:[0-9a-zA-Z-]+\.)?(?:twitter\.com/\S*)/(status|statuses)/(\d*)""".r

  val youTubeIdRegex = """https?://(?:[0-9a-zA-Z-]+\.)?(?:youtu\.be/|youtube\.com\S*[^\w\-\s])([\w \-]{11})(?=[^\w\-]|$)(?![?=&+%\w]*(?:[\'"][^<>]*>|</a>))[?=&+%\w-]*""".r

  val vineVideoUrlRegex = """^(https?://)?(?:[0-9a-zA-Z-]+\.)?(?:vine\.co)/(v)/(.*)""".r

  def twitterStatusId(url: String): Option[String] =
    twitterStatusUrlRegex.findFirstMatchIn(url).map(m => { val count = m.groupCount; m.group(count) })

  def vineVideoId(url: String): Option[String] =
    vineVideoUrlRegex.findFirstMatchIn(url).map(m => { val count = m.groupCount; m.group(count) })

  def youtubeVideoId(url: String): Option[String] =
    youTubeIdRegex.findFirstMatchIn(url).map(m => { val count = m.groupCount; m.group(count) })

  def grasswireUserMention(username: String) = s"(?i)(@)?$username".r

}

