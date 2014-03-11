name := "hystrix-scratchpad"

organization := "com.beachape"

version := "0.0.1"

scalaVersion := "2.10.3"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test" withSources() withJavadoc(),
  "org.scalacheck" %% "scalacheck" % "1.10.0" % "test" withSources() withJavadoc(),
  "com.netflix.hystrix" % "hystrix-core" % "1.3.13",
  "com.netflix.rxjava" % "rxjava-scala" % "0.16.1",
  "io.spray" % "spray-client" % "1.3.0",
  "commons-validator" % "commons-validator" % "1.4.0",
  "io.spray" %%  "spray-json" % "1.2.5",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.3.0"
)

scalacOptions ++= Seq("-deprecation", "-feature")

initialCommands := "import com.beachape.hystrixscratchpad._"