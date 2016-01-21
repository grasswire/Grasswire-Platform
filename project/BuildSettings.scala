import sbt._
import Keys._

object BuildSettings {
  val VERSION = "1.1.13"
  lazy val basicSettings = Seq(
    triggeredMessage := Watched.clearWhenTriggered,
    version := VERSION,
    exportJars := true,
    sbtVersion in Global := Dependencies.sbtVersion,
    scalaVersion in Global := Dependencies.myScalaVersion,
    autoScalaLibrary := false,
    offline := false,
    organization := "com.grasswire",
    homepage := Some(url("http://www.graswire.com")),
    startYear := Some(2013),
    description := "Grasswire Platform",
    scalacOptions ++= Seq(
      "-encoding", "UTF-8" ,
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",  
      "-Ywarn-value-discard"
    ),
    javaOptions in Test ++= Seq(
      "-Xmx2048m", "-XX:MaxPermSize=512m",
      "-Djava.library.path=lib"
    ),
    Keys.fork in run := true,
    Keys.fork in Test := false,
    shellPrompt := ShellPrompt.buildShellPrompt,
    exportJars := true,
    parallelExecution in Test := true,
    externalResolvers <<= resolvers map { rs =>
      Resolver.withDefaultResolvers(rs, mavenCentral = true) ++ Dependencies.gwResolvers
    },
   publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

   )
}
