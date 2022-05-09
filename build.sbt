val Http4sVersion = "0.21.33"
val CirceVersion = "0.14.1"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.3.0-alpha14"
val MunitCatsEffectVersion = "1.0.7"
val SttpVersion = "3.3.9"
val DoobieVersion = "0.13.4"
val CalibanVersion = "1.1.0"

lazy val root = (project in file("."))
  .enablePlugins(JavaServerAppPackaging)
  .enablePlugins(DockerPlugin)
  .settings(
    organization := "moe.lordie",
    name := "nytaggr",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    javaOptions ++= Seq(
      "-Dquill.binds.log=true",
      "-Dquill.macro.log.pretty=true"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta"                 %% "munit"               % MunitVersion           % Test,
      "org.typelevel"                 %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
      "org.http4s"                    %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"                    %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"                    %% "http4s-circe"        % Http4sVersion,
      "org.http4s"                    %% "http4s-dsl"          % Http4sVersion,
      "io.circe"                      %% "circe-generic"       % CirceVersion,
      "io.circe"                      %% "circe-optics"        % CirceVersion,
      "ch.qos.logback"                %  "logback-classic"     % LogbackVersion,
      "com.github.ghostdogpr"         %% "caliban"             % CalibanVersion,
      "com.github.ghostdogpr"         %% "caliban-http4s"      % CalibanVersion,
      "com.github.ghostdogpr"         %% "caliban-cats"        % CalibanVersion,
      "org.tpolecat"                  %% "doobie-quill"        % DoobieVersion,
      "org.tpolecat"                  %% "doobie-hikari"       % DoobieVersion,
      "org.tpolecat"                  %% "doobie-postgres"     % DoobieVersion,
      "com.softwaremill.sttp.client3" %% "http4s-ce2-backend"  % SttpVersion,
      "com.softwaremill.sttp.client3" %% "circe"               % SttpVersion,
      "org.scalameta"                 %% "svm-subs"            % "20.2.0",
      "io.monix"                      %% "monix"               % "3.4.0",
      "dev.zio"                       %% "zio"                 % "1.0.14",
      "dev.zio"                       %% "zio-interop-cats"    % "2.5.1.0",
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework"),

    dockerBaseImage := "ghcr.io/graalvm/graalvm-ce:latest",
    dockerExposedPorts ++= Seq(3000),
    dockerUpdateLatest := true,
    dockerRepository := Some("ghcr.io/chaoky"),
    dockerLabels := Map("org.opencontainers.image.source" -> "https://github.com/chaoky/nytaggr"),
  )
