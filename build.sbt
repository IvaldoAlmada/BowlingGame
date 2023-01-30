
lazy val root = (project in file("."))
  .settings(
    name := "BowlingGame",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := "2.13.10",
    organization := "com.game.bowling",
    Compile / mainClass := Some("com.game.bowling.HttpServer"),
    assembly / assemblyJarName := "BowlingGame.jar"
  )

val Http4sVersion = "1.0.0-M21"
val CirceVersion = "0.14.3"
val DoobieVersion = "1.0.0-RC1"
val NewTypeVersion = "0.4.4"

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,

  "io.circe" %% "circe-generic" % CirceVersion,

  //Test
  "org.scalactic" %% "scalactic" % "3.2.15",
  "org.scalatest" %% "scalatest" % "3.2.15" % Test,
  "org.scalatestplus" %% "mockito-3-4" % "3.2.10.0" % Test,

  //database
  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari" % DoobieVersion,
  "io.estatico" %% "newtype" % NewTypeVersion
)