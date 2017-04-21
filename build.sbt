name := "miragesql-scala"
organization := "com.miragesql"
version := "0.3.0"
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "jp.sf.amateras" % "mirage" % "1.2.5" % "compile",
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

pomExtra := (
  <url>https://github.com/mirage-sql/mirage-scala</url>
    <licenses>
      <license>
        <name>The Apache Software License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <url>https://github.com/mirage-sql/mirage-scala</url>
      <connection>scm:git:https://github.com/mirage-sql/mirage-scala.git</connection>
    </scm>
    <developers>
      <developer>
        <id>takezoe</id>
        <name>Naoki Takezoe</name>
        <email>takezoe_at_gmail.com</email>
        <timezone>+9</timezone>
      </developer>
    </developers>
  )