enablePlugins(
  BuildInfoPlugin,
  GraalVMNativeImagePlugin,
)

name         := "jetbrains-flake-updater"
description  := "Update JetBrains product versions in jetbrains-flake."
version      := "1"
scalaVersion := "3.1.2"

buildInfoKeys    := Seq[BuildInfoKey](name, version, description)
buildInfoPackage := "updater"

scalacOptions ++= Seq(
  "-deprecation",
//  "-explain",
  "-feature",
  "-new-syntax",
  "-source",
  "future",
  "-release",
  "17",
  "-unchecked",
  "-Wunused:all",
  "-Xcheck-macros",
  "-Xverify-signatures",
  "-Ycook-docs",
  "-Ysafe-init",
//  "-Yexplicit-nulls",
  "-Ykind-projector:underscores",
)

libraryDependencies ++= Seq(
  "org.scala-lang.modules" %% "scala-xml"           % "2.1.0",
  "co.fs2"                 %% "fs2-io"              % "3.2.7",
  "org.typelevel"          %% "cats-core"           % "2.7.0",
  "org.typelevel"          %% "cats-effect"         % "3.3.11",
  "org.typelevel"          %% "cats-time"           % "0.5.0",
  "io.circe"               %% "circe-core"          % "0.15.0-M1",
  "io.circe"               %% "circe-jawn"          % "0.15.0-M1",
  "io.circe"               %% "circe-generic"       % "0.15.0-M1",
  "io.circe"               %% "circe-fs2"           % "0.14.0",
  "org.slf4j"               % "slf4j-api"           % "1.7.36",
  "org.slf4j"               % "slf4j-simple"        % "1.7.36" % Runtime,
  "org.http4s"             %% "http4s-scala-xml"    % "1.0.0-M32",
  "org.http4s"             %% "http4s-circe"        % "1.0.0-M32",
  "org.http4s"             %% "http4s-ember-client" % "1.0.0-M32",
  "org.http4s"             %% "http4s-dsl"          % "1.0.0-M32",
  "com.monovore"           %% "decline-effect"      % "2.2.0",
  "org.scalameta"          %% "munit"               % "0.7.29" % Test,
  "org.scalameta"          %% "munit-scalacheck"    % "0.7.29" % Test,
  "org.typelevel"          %% "munit-cats-effect-3" % "1.0.7"  % Test,
  "org.typelevel"          %% "discipline-munit"    % "1.0.9"  % Test,
  "org.typelevel"          %% "cats-laws"           % "2.7.0"  % Test,
)

graalVMNativeImageOptions ++= Seq(
  "--no-fallback",
  "-H:+ReportExceptionStackTraces",
  "--allow-incomplete-classpath",
  "--initialize-at-build-time",
)
