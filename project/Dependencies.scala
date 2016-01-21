import sbt._

object Dependencies {

  val gwResolvers = Seq(
    "spray repo" at "http://repo.spray.io",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe snapshots" at "http://repo.typesafe.com/typesafe/snapshots/",
    "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases",
    "slick migration bintray repo" at "http://dl.bintray.com/naftoligug/maven")

  def compile(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")

  //sbt/scala versions
  val sbtVersion = "0.13.5"
  val myScalaVersion = "2.11.6"

  val sprayVersion = "1.3.3"
  val json4sVersion = "3.2.11"
  val scalazVersion = "7.1.2"

  //logging and config
  val slf4j = "org.slf4j" % "slf4j-api" % "1.7.7"
  val logback = "ch.qos.logback" % "logback-classic" % "1.0.13"
  val typesafeConfig = "com.typesafe" % "config" % "1.2.1"

  //spray
  val sprayCan = "io.spray" %% "spray-can" % sprayVersion
  val sprayRouting = "io.spray" %% "spray-routing" % sprayVersion
  val sprayClient = "io.spray" %% "spray-client" % sprayVersion
  val sprayUtil = "io.spray" %% "spray-util" % sprayVersion
  val sprayCache = "io.spray" %% "spray-caching" % sprayVersion
  val sprayHttpx = "io.spray" %% "spray-httpx" % sprayVersion
  val sprayDeps = Seq(sprayRouting, sprayCan, sprayUtil, sprayHttpx, sprayCache)


  //json4s
  val json4sNative = "org.json4s" %% "json4s-native" % json4sVersion
  val json4sext = "org.json4s" %% "json4s-ext" % json4sVersion


  //scalaz
  val `scalaz-stream` = "org.scalaz.stream" %% "scalaz-stream" % "0.7a" withSources()
  val scalaz = "org.scalaz" %% "scalaz-core" % scalazVersion
  val `scalaz-concurrent` = "org.scalaz" %% "scalaz-concurrent" % scalazVersion

  //slick and db
  val slickVersion = "3.1.0"
  val slick = "com.typesafe.slick" %% "slick" % slickVersion withSources()
  val slickHickaricp = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion withSources()
  val postgresdriver = "org.postgresql" % "postgresql" % "9.4-1204-jdbc42"
  val hikariCP = "com.zaxxer" % "HikariCP" % "2.4.1"

  //joda
  val jodaTime = "joda-time" % "joda-time" % "2.3"
  val jodaConvert = "org.joda" % "joda-convert" % "1.6"

  val bcrypt = "org.mindrot" % "jbcrypt" % "0.3m"

  val rabbitMQClient = "com.rabbitmq" % "amqp-client" % "3.3.1"
  val scredis = "com.livestream" %% "scredis" % "2.0.6" withSources()
  val sendgrid = "com.sendgrid" % "sendgrid-java" % "1.2.0"

  //swagger
  val spraySwagger = "com.gettyimages" %% "spray-swagger" % "0.5.0"
  val swaggerCore = "com.wordnik" %% "swagger-core" % "1.3.10" excludeAll(ExclusionRule(organization = "org.json4s"), ExclusionRule(organization = "org.fasterxml*"))
  val playJson = "com.typesafe.play" %% "play-json" % "2.3.8"

  //testing
  val scalatest = "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

  val slackWebhook = "net.gpedro.integrations.slack" % "slack-webhook" % "1.0.4-SNAPSHOT"

  val playJsonVariants = "org.julienrf" %% "play-json-variants" % "1.1.0"

  val logEntries = "com.logentries" % "logentries-appender" % "1.1.30"
}
