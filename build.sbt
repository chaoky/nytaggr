val Http4sVersion = "0.21.33"
val CirceVersion = "0.14.1"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.3.0-alpha14"
val MunitCatsEffectVersion = "1.0.7"

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
      "org.http4s"                    %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"                    %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"                    %% "http4s-circe"        % Http4sVersion,
      "org.http4s"                    %% "http4s-dsl"          % Http4sVersion,
      "io.circe"                      %% "circe-generic"       % CirceVersion,
      "io.circe"                      %% "circe-optics"        % CirceVersion,
      "org.scalameta"                 %% "munit"               % MunitVersion           % Test,
      "org.typelevel"                 %% "munit-cats-effect-2" % MunitCatsEffectVersion % Test,
      "ch.qos.logback"                %  "logback-classic"     % LogbackVersion,
      "org.scalameta"                 %% "svm-subs"            % "20.2.0",
      "io.monix"                      %% "monix"               % "3.4.0",
      "dev.zio"                       %% "zio"                 % "1.0.14",
      "dev.zio"                       %% "zio-interop-cats"    % "2.5.1.0",
      "org.sangria-graphql"           %% "sangria"             % "2.0.0",
      "org.sangria-graphql"           %% "sangria-circe"       % "1.3.0",
      "com.github.ghostdogpr"         %% "caliban"             % "1.1.0",
      "com.github.ghostdogpr"         %% "caliban-http4s"      % "1.1.0",
      "com.github.ghostdogpr"         %% "caliban-cats"        % "1.1.0",
      "co.fs2"                        %% "fs2-core"            % "2.5.10",
      "co.fs2"                        %% "fs2-io"              % "2.5.10",
      "org.tpolecat"                  %% "doobie-quill"        % "0.13.4",
      "org.tpolecat"                  %% "doobie-hikari"       % "0.13.4",
      "org.tpolecat"                  %% "doobie-postgres"     % "0.13.4",
      "com.softwaremill.sttp.client3" %% "http4s-ce2-backend"  % "3.3.9",
      "com.softwaremill.sttp.client3" %% "circe"               % "3.3.9"



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
