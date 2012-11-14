name := "mirage-scala"

organization := "jp.sf.amateras.mirage"

version := "0.0.5"

//scalaVersion := "2.9.0"

crossScalaVersions := Seq("2.8.1", "2.9.1", "2.9.1-1", "2.9.2")

resolvers += "amateras-release-repo" at "http://amateras.sourceforge.jp/mvn/"

resolvers += "amateras-snapshot-repo" at "http://amateras.sourceforge.jp/mvn-snapshot/"

resolvers += "Local Maven Repository" at "file:///"+Path.userHome.absolutePath+"/.m2/repository"

libraryDependencies ++= Seq(
  "jp.sf.amateras.mirage" % "mirage" % "1.1.6" % "compile",
  "org.hsqldb" % "hsqldb" % "2.0.0" % "test",
  "org.scala-tools.testing" % "specs_2.8.1" % "1.6.8" % "test",
  "org.mockito" % "mockito-core" % "1.8.5" % "test"
)

libraryDependencies <+= scalaVersion("org.scala-lang" % "scalap" % _ % "provided")

//publishTo := Some(Resolver.ssh("amateras-repo-scp", "shell.sourceforge.jp", "/home/groups/a/am/amateras/htdocs/mvn/")
//  as(System.getProperty("user.name"), new java.io.File(Path.userHome.absolutePath + "/.ssh/id_rsa")))

publishTo <<= (version) { version: String =>
  val repoInfo =
    if (version.trim.endsWith("SNAPSHOT"))
      ("amateras snapshots" -> "/home/groups/a/am/amateras/htdocs/mvn-snapshot/")
    else
      ("amateras releases" -> "/home/groups/a/am/amateras/htdocs/mvn/")
  Some(Resolver.ssh(
    repoInfo._1,
    "shell.sourceforge.jp",
    repoInfo._2) as(System.getProperty("user.name"), (Path.userHome / ".ssh" / "id_rsa").asFile) withPermissions("0664"))
}