// import AssemblyKeys._

// assemblySettings

name := "addbug"

version := "1.0.0"

organization := "apl"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.4.0" % "provided",
  "org.apache.spark" %% "spark-mllib" % "1.4.0" % "provided",
  "com.github.scopt" %% "scopt" % "3.2.0"
)


resolvers += "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

// addSbtPlugin("org.ensime" % "ensime-sbt" % "0.1.5-SNAPSHOT")

publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblyMergeStrategy in assembly := {
  // case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  // case "META-INF/MANIFEST.MF" => MergeStrategy.last
  case k =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(k)
}
