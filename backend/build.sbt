ThisBuild / version := "1.0.0"
ThisBuild / scalaVersion := "3.3.1"

lazy val root = (project in file("."))
  .settings(
    name := "guitar-chord-backend",
    libraryDependencies ++= Seq(
      // Netty for HTTP server
      "io.netty" % "netty-all" % "4.1.100.Final",
      
      // JSON handling
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.14.6",
      "io.circe" %% "circe-parser" % "0.14.6",
      
      // Database connectivity
      "org.postgresql" % "postgresql" % "42.6.0",
      "com.zaxxer" % "HikariCP" % "5.0.1",
      
      // Configuration
      "com.typesafe" % "config" % "1.4.3",
      
      // Logging
      "ch.qos.logback" % "logback-classic" % "1.4.11",
      "org.slf4j" % "slf4j-api" % "2.0.9",
      
      // AWS SDK for Secrets Manager
      "software.amazon.awssdk" % "secretsmanager" % "2.20.162",
      "software.amazon.awssdk" % "auth" % "2.20.162",
      
      // Database migration
      "org.flywaydb" % "flyway-core" % "9.16.3",
      
      // Testing
      "org.scalatest" %% "scalatest" % "3.2.17" % Test
    ),
    
    // Compiler options
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wunused:all",
      "-Wvalue-discard"
    ),
    
    // Assembly plugin for fat JAR
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },
    
    // Main class
    assembly / mainClass := Some("com.guitarvisualizer.Main"),
    
    // Flyway configuration
    flywayUrl := "jdbc:postgresql://localhost:5432/guitar_chords",
    flywayUser := "chorduser",
    flywayPassword := "chordpass",
    flywayLocations := Seq("classpath:db/migration")
  )
