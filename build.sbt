name := "almoneya"

version := "00001"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature")

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
    "ch.qos.logback"                % "logback-classic"      % "[1.1.2,)",
    "com.fasterxml.jackson.core"    % "jackson-core"         % "2.7.4",
    "com.fasterxml.jackson.core"    % "jackson-databind"     % "2.7.4",
    "com.fasterxml.jackson.core"    % "jackson-annotations"  % "2.7.4",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.7.3",
    "joda-time"                     % "joda-time"            % "[2.1,3.0[",
    "org.apache.commons"            % "commons-csv"          % "1.2",
    "org.eclipse.jetty"             % "jetty-server"         % "9.3.8.v20160314",
    "org.eclipse.jetty"             % "jetty-plus"           % "9.3.8.v20160314",
    "org.eclipse.jetty"             % "jetty-annotations"    % "9.3.8.v20160314",
    "org.joda"                      % "joda-convert"         % "[1.3,2.0[",
    "org.mindrot"                   % "jbcrypt"              % "0.3m",
    "org.postgresql"                % "postgresql"           % "9.4.1208.jre7",
    "org.postgresql"                % "postgresql"           % "9.4.1208.jre7",

    "org.scalacheck"               %% "scalacheck"           % "[1.11.4,2.0[" % "test",
    "org.scalatest"                %% "scalatest"            % "[2.1.6,3.0["  % "test",
    "junit"                         %  "junit"               % "[4.11,5.0["   % "test",
    "com.novocode"                  %  "junit-interface"     % "[0.11,1.0["   % "test"
)
