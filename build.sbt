enablePlugins(
  BuildInfoPlugin,
  JavaAppPackaging,
)

name         := "jetbrains-flake-updater"
description  := "Update JetBrains product versions in jetbrains-flake."
maintainer   := "liff@iki.fi"
version      := "1"
scalaVersion := "3.2.0"

buildInfoKeys    := Seq[BuildInfoKey](name, version, description)
buildInfoPackage := "updater"

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-new-syntax",
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
  "co.fs2"                 %% "fs2-io"              % "3.3.0",
  "org.typelevel"          %% "cats-core"           % "2.8.0",
  "org.typelevel"          %% "cats-effect"         % "3.3.14",
  "org.typelevel"          %% "cats-time"           % "0.5.0",
  "io.circe"               %% "circe-core"          % "0.14.3",
  "io.circe"               %% "circe-jawn"          % "0.14.3",
  "io.circe"               %% "circe-generic"       % "0.14.3",
  "io.circe"               %% "circe-fs2"           % "0.14.0",
  "org.slf4j"               % "slf4j-api"           % "2.0.3",
  "org.slf4j"               % "slf4j-simple"        % "2.0.3" % Runtime,
  "org.http4s"             %% "http4s-scala-xml"    % "1.0.0-M35",
  "org.http4s"             %% "http4s-circe"        % "1.0.0-M35",
  "org.http4s"             %% "http4s-ember-client" % "1.0.0-M35",
  "org.http4s"             %% "http4s-dsl"          % "1.0.0-M35",
  "com.monovore"           %% "decline-effect"      % "2.3.1",
)
