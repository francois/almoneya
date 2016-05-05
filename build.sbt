name := "almoneya"

version := "00001"

scalaVersion := "2.11.8"

// Enables use of System.exit in user code when running using sbt
// http://www.scala-sbt.org/0.13/docs/Forking.html
fork in run := true

resolvers ++= Seq(
    "Sonatype Releases" at "http://oss.sonatype.org/content/repositories/releases",
    "spray" at "http://repo.spray.io/"
)

///////////////////////////////////////////////////
// DEPENDENCIES

libraryDependencies ++= Seq(
    "ch.qos.logback"       %  "logback-classic"  % "[1.1.2,)",
    "joda-time"            %  "joda-time"        % "[2.1,3.0[",
    "org.joda"             %  "joda-convert"     % "[1.3,2.0[",
    "org.mindrot"          % "jbcrypt"           % "0.3m",
    "org.postgresql"       %  "postgresql"       % "9.4.1208.jre7",
    "org.postgresql"       %  "postgresql"       % "9.4.1208.jre7",

    "org.scalacheck" %% "scalacheck"      % "[1.11.4,2.0[" % "test",
    "org.scalatest"  %% "scalatest"       % "[2.1.6,3.0["  % "test",
    "junit"          %  "junit"           % "[4.11,5.0["   % "test",
    "com.novocode"   %  "junit-interface" % "[0.11,1.0["   % "test"
)
