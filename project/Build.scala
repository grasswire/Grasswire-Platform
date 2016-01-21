import com.typesafe.sbt.packager.Keys._
import sbt.Keys._
import sbt._
import sbtassembly.Plugin.AssemblyKeys._
import play._
import play.Play.autoImport._
import spray.revolver.RevolverPlugin.Revolver
//import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd, Dockerfile, DockerPlugin}
//import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker


object GwBuild extends Build {

  import Dependencies._
  import ProjectDefs._

  val stage = sys.props.getOrElse("stage", default = "stage")
  println("release stage is set to: " + stage)

  lazy val root = gwRootProject(common, website, users, email, api).enablePlugins(PlayScala)

  lazy val api = gwProject("api")(ws, slackWebhook)
    .dependsOn(common % "compile->compile;test->test", users % "compile->compile;test->test")
    .settings(Revolver.settings: _*)
    .settings(dockerExposedPorts := Seq(8080))

  lazy val sslRedirect = gwProject("ssl_redirect")()
    .dependsOn(common % "compile->compile;test->test", users % "compile->compile;test->test")
    .settings(dockerExposedPorts := Seq(8090))

  lazy val users = gwProject("users")()
    .aggregate(common, email)
    .dependsOn(common % "compile->compile;test->test", email % "compile->compile;test->test")

  lazy val email = gwProject("email")(sendgrid)
    .aggregate(common)
    .dependsOn(common % "compile->compile;test->test")

  lazy val common = gwProject("common")(
    Seq(scalaz, `scalaz-concurrent`, `scalaz-stream`, slf4j, typesafeConfig, scalaz, scredis,
      slick, slickHickaricp, postgresdriver, json4sNative, json4sext, jodaTime, swaggerCore, spraySwagger,
      jodaConvert, bcrypt, rabbitMQClient, playJson, ws, hikariCP, scalatest, playJsonVariants, logEntries) ++ sprayDeps: _*)

  lazy val website = Project(id = "website", base = file("website")).settings(
    name := "website",
    dockerRepository := Some("levinotik"),
    version in Docker := stage,
    version := "1",
    PlayKeys.playWatchService := play.runsupport.PlayWatchService.sbt(1000),
    scalaVersion := Dependencies.myScalaVersion,
    libraryDependencies ++= Seq(
    logEntries,
      filters,
      cache,
      ws,
      "com.twitter" % "twitter-text" % "1.12.1",
      "com.mohiva" %% "play-html-compressor" % "0.3",
      "com.wordnik" %%	"swagger-play2" %	"1.3.12",
      "com.wordnik" %%	"swagger-play2-utils" %	"1.3.12")
  ).enablePlugins(PlayScala)
    .dependsOn(common % "compile->compile;test->test")

}
