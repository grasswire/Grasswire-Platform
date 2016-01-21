import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import play.PlayScala
import sbt._
import Keys._
import spray.revolver.RevolverPlugin._
import sbtassembly.Plugin._
import sbtassembly.Plugin.AssemblyKeys._
import com.typesafe.sbt.SbtScalariform.scalariformSettings
import com.typesafe.sbt.SbtNativePackager.autoImport._
import com.typesafe.sbt.packager.docker.{Dockerfile, DockerPlugin}
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.docker.DockerPlugin
import com.typesafe.sbt.packager.docker.DockerPlugin.autoImport.Docker


object ProjectDefs {

  import BuildSettings._
  import Dependencies._

  def gwRootProject(subProjects: ProjectReference*) = Project(
    "root",
    file(".")
  ).aggregate(subProjects.toSeq: _*)
    .settings(basicSettings: _*)

  val projectMergeStrategy = mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
    case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
    case PathList("javax", "servlet", xs@_*) => MergeStrategy.first
    case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
    case "application.conf" => MergeStrategy.concat
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
  }

  def gwProject(projectName: String)(otherDependencies: ModuleID*): Project = {
    Project(
      id = projectName,
      base = file(projectName),
      settings = Defaults.coreDefaultSettings ++ scalariformSettings
    ).settings(
        libraryDependencies ++= otherDependencies,
        resolvers ++= gwResolvers
      ).settings(basicSettings: _*)
      .settings(sbtassembly.Plugin.assemblySettings: _*)
      .settings(projectMergeStrategy, unmanagedResourceDirectories in Compile += {
      baseDirectory.value / "src/main/resources"
    })
      .settings(jarName in assembly := projectName + ".jar",
        dockerRepository := Some("levinotik"),
        version in Docker := sys.props.getOrElse("stage", default = "stage"))
      .enablePlugins(JavaAppPackaging, DockerPlugin)
  }
}
