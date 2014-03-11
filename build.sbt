name := "hystrix-scratchpad"

organization := "com.beachape"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources() withJavadoc(),
   "com.netflix.hystrix" % "hystrix-core" % "1.3.9"
)

initialCommands := "import com.beachape.hystrixscratchpad._"

