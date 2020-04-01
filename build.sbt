import Dependencies._

name := "ESPBench"
val flinkVersion: String = "1.8.2"
val sparkVersion: String = "2.4.4"
val beamVersion: String = "2.16.0"
val beamRunner: String = BeamRunner.SPARK //defines which system to benchmark w/ the beam implementation

lazy val commonSettings = Seq(
  organization := "org.hpi",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.11.12",
  test in assembly := {},
  logBuffered in test := false,
  parallelExecution in Test := false,
  scalacOptions ++= Seq("-Xmax-classfile-name", "78")
)

lazy val root = (project in file(".")).
  settings(commonSettings).
  aggregate(datasender, validator, beam, tpccDatagen, util, commons)

lazy val commons = (project in file("tools/commons")).
  settings(commonSettings,
    name := "Commons",
    libraryDependencies ++= pureConfig,
    libraryDependencies ++= logging,
    libraryDependencies ++= csv,
    libraryDependencies ++= testUtils
  )

lazy val datasender = (project in file("tools/datasender")).
  settings(commonSettings,
    name := "DataSender",
    mainClass in assembly := Some("org.hpi.esb.datasender.Main"),
    libraryDependencies ++= kafkaClients,
    libraryDependencies ++= slick
  ).
  dependsOn(util, commons % "test->test;compile->compile")

lazy val validator = (project in file("tools/validator")).
  settings(commonSettings,
    name := "Validator",
    mainClass in Compile := Some("org.hpi.esb.datavalidator.Main"),
    libraryDependencies ++= metrics,
    libraryDependencies ++= akka,
    libraryDependencies ++= slick,
    libraryDependencies ++= stochasticOutlierDetection
  ).
  dependsOn(util, commons % "test->test;compile->compile")

lazy val util = (project in file("tools/util")).
  settings(commonSettings,
    name := "Util",
    mainClass in(Compile, run) := Some("org.hpi.esb.util.Main"),
    libraryDependencies ++= kafka,
    libraryDependencies ++= json,
    libraryDependencies ++= scopt,
    libraryDependencies += "org.slf4j" % "slf4j-simple" % "1.7.28"
  ).
  dependsOn(commons % "test->test;compile->compile")

lazy val tpccDatagen = (project in file("tools/tpc-c_gen")).
  settings(commonSettings,
    name := "TPC-CDataGenerator",
    mainClass in(Compile, run) := Some("org.hpi.esb.tpccdatagen.Main"),
  ).
  dependsOn(util)

lazy val beam = (project in file("implementation/beam")).
  settings(commonSettings,
    name := "Beam",
    mainClass in assembly := Some("org.hpi.esb.beam.JobRunner"),
    publishMavenStyle := true,
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a")),
    libraryDependencies ++= Dependencies.beam(beamVersion),
    libraryDependencies ++= {
      beamRunner match {
        case BeamRunner.SPARK => sparkRunnerDependencies(beamVersion, sparkVersion)
        case BeamRunner.FLINK => flinkRunnerDependencies(beamVersion, flinkVersion)
        case BeamRunner.HAZELCAST_JET => hazelcastJetRunnerDependencies(beamVersion)
        case x => directRunner(beamVersion)
      }
    },
    libraryDependencies ++= testUtilsJava,
    assemblyMergeStrategy in assembly ~= { old => {
      case s if s.endsWith(".properties") => MergeStrategy.filterDistinctLines
      case s if s.endsWith("pom.xml") => MergeStrategy.last
      case s if s.endsWith(".class") => MergeStrategy.last
      case s if s.endsWith(".proto") => MergeStrategy.last
      case s if s.endsWith("libjansi.jnilib") => MergeStrategy.last
      case s if s.endsWith("jansi.dll") => MergeStrategy.rename
      case s if s.endsWith("libjansi.so") => MergeStrategy.rename
      case s if s.endsWith("libsnappyjava.jnilib") => MergeStrategy.last
      case s if s.endsWith("libsnappyjava.so") => MergeStrategy.last
      case s if s.endsWith("snappyjava_snappy.dll") => MergeStrategy.last
      case s if s.endsWith(".dtd") => MergeStrategy.rename
      case s if s.endsWith(".xsd") => MergeStrategy.rename
      case PathList("META-INF", "services", "org.apache.hadoop.fs.FileSystem") =>
        MergeStrategy.filterDistinctLines
      case s => old(s)
    }
    }
  ).
  dependsOn(commons)
