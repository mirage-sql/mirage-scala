name := "mirage-scala"
organization := "jp.sf.amateras.mirage"
version := "0.2.0-SNAPSHOT"
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "jp.sf.amateras" % "mirage" % "1.2.4" % "compile",
  "org.json4s" %% "json4s-scalap" % "3.5.0",
  "org.hsqldb" % "hsqldb" % "2.0.0" % "test",
  "org.specs2" %% "specs2-core" % "3.8.9" % "test",
  "org.mockito" % "mockito-core" % "1.8.5" % "test"
)

scalacOptions := Seq("-deprecation", "-feature")

parallelExecution in Test := false

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}
