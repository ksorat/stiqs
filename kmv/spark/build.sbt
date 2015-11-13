// import AssemblyKeys._

// assemblySettings

name := "kmv"

version := "1.0.0"

organization := "apl"

scalaVersion := "2.10.5" //Seems to have to be 2.10.x for spark to work

//libraryDependencies += "com.github.fommil.netlib" % "all" % "1.1.2" pomOnly()
/*libraryDependencies += "com.github.fommil.netlib" % "all"  % "1.1.2" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.1" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-mllib" % "1.5.1" % "provided"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"*/
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.5.1" % "provided",
  "org.apache.spark" %% "spark-mllib" % "1.5.1" % "provided",
  "com.github.scopt" %% "scopt" % "3.2.0"
)


resolvers += "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"


publishTo := Some(Resolver.file("file",  new File(Path.userHome.absolutePath+"/.m2/repository")))

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))

assemblyMergeStrategy in assembly := {
  // case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  // case "META-INF/MANIFEST.MF" => MergeStrategy.last
  case k =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(k)
}
