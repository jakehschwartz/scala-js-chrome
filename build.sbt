inThisBuild(List(
  organization := "com.alexitc",
  homepage := Some(url("https://github.com/AlexITC/scala-js-chrome")),
  licenses := List("MIT" -> url("http://www.opensource.org/licenses/mit-license.html")),
  developers := List(
    Developer(
      "AlexITC",
      "Alexis Hernandez",
      "alexis22229@gmail.com",
      url("https://wiringbits.net")
    )
  )
))

skip in publish := true

lazy val commonSettings = Seq(
  scalacOptions ++= Seq(
    "-Xlint",
    "-deprecation",
    "-Xfatal-warnings",
    "-feature"
  ), unmanagedSourceDirectories in Compile ++= Seq(
    baseDirectory.value.getParentFile / "shared" / "src" / "main" / "scala"
  ),
)

lazy val commonPlugins = Seq()

lazy val bindings = project.in(file("bindings"))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-js-chrome",
    crossScalaVersions := Seq("2.12.10", "2.13.1"),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "1.0.0"
    ),
    scalaJSUseMainModuleInitializer := true
  ).
  enablePlugins(commonPlugins: _*).
  enablePlugins(ScalaJSPlugin).
  enablePlugins(JSDependenciesPlugin)

lazy val plugin = project.in(file("sbt-plugin")).
  settings(commonSettings: _*).
  settings(
    sbtPlugin := true,
    name := "sbt-chrome-plugin",
    libraryDependencies ++= {
      // NOTE: Avoid circe as it doesn't respect binary compatibility which causes lots of issues
      Seq(
        "com.lihaoyi" %%% "upickle" % "1.0.0",
        "org.scalactic" %% "scalactic" % "3.1.1" % "test",
        "org.scalatest" %% "scalatest" % "3.1.1" % "test"
      )
    },
    addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.0.1"),
    addSbtPlugin("org.scala-js" % "sbt-jsdependencies" % "1.0.0")
  ).
  enablePlugins(commonPlugins: _*)
