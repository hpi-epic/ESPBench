import sbt._

object Dependencies {

  object BeamRunner {
    val SPARK = "SparkRunner"
    val FLINK = "FlinkRunner"
    val HAZELCAST_JET = "JetRunner"
  }

  val testUtils: Seq[ModuleID] = Seq(
    "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % Test,
    "org.mockito" %% "mockito-scala" % "1.5.13" % Test,
    "org.scalactic" %% "scalactic" % "3.0.4" % Test,
    "org.scalatest" %% "scalatest" % "3.0.4" % Test
  )

  val testUtilsJava: Seq[ModuleID] = Seq(
    "junit" % "junit" % "4.12" % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test exclude("junit", "junit-dep")
  )

  val slick: Seq[ModuleID] = Seq(
    "com.typesafe.slick" %% "slick" % "3.3.1",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.3.1",
    "org.postgresql" % "postgresql" % "42.2.5"
  )

  val kafkaClients: Seq[ModuleID] = Seq(
    "org.apache.kafka" % "kafka-clients" % "2.3.0"
  )

  val kafka: Seq[ModuleID] = Seq(
    "org.apache.kafka" %% "kafka" % "2.3.0"
  )

  val logging: Seq[ModuleID] = Seq(
    "log4j" % "log4j" % "1.2.14"
  )

  val scalaIO: Seq[ModuleID] = Seq(
    "com.github.scala-incubator.io" %% "scala-io-core" % "0.4.3-1",
    "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.3-1"
  )

  val pureConfig: Seq[ModuleID] = Seq(
    "com.github.pureconfig" %% "pureconfig" % "0.11.1"
  )

  val scopt: Seq[ModuleID] = Seq(
    "com.github.scopt" %% "scopt" % "3.5.0"
  )

  val metrics: Seq[ModuleID] = Seq(
    "io.dropwizard.metrics" % "metrics-core" % "3.1.0"
  )

  val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.24",
    "com.typesafe.akka" %% "akka-stream-kafka" % "1.0.5",
    "com.typesafe.akka" %% "akka-stream-testkit" % "2.5.25" % Test
  )

  val csv: Seq[ModuleID] = Seq(
    "com.github.tototoshi" %% "scala-csv" % "1.3.4"
  )

  val json: Seq[ModuleID] = Seq(
    "net.liftweb" %% "lift-json" % "3.0.1"
  )

  val stochasticOutlierDetection: Seq[ModuleID] = Seq(
    "com.github.gnni" %% "scala-stochastic-outlier-selection" % "0.2.0"
  )

  def sparkRunnerDependencies(beamVersion: String, sparkVersion: String): Seq[ModuleID] = Seq(
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.10.0" % Test //breaks hazelcast jet jobs
  )

  def flinkRunnerDependencies(beamVersion: String, flinkVersion: String): Seq[ModuleID] = Seq(
    "org.apache.beam" % "beam-runners-flink-1.8" % beamVersion
  )

  def hazelcastJetRunnerDependencies(beamVersion: String): Seq[ModuleID] = Seq(
  )

  def directRunner(beamVersion: String): Seq[ModuleID] = Seq(
    "org.apache.beam" % "beam-runners-direct-java" % beamVersion
  )

  def beam(beamVersion: String): Seq[ModuleID] = Seq(
    "org.apache.beam" % "beam-sdks-java-core" % beamVersion,
    "org.apache.beam" % "beam-sdks-java-io-kafka" % beamVersion,
    "org.postgresql" % "postgresql" % "42.2.6",
    "com.github.gnni" %% "scala-stochastic-outlier-selection" % "0.2.0",
    "org.apache.kafka" % "kafka-clients" % "2.3.0", //for Serializer/Deserializer classes
    "org.hamcrest" % "hamcrest-all" % "1.3" % Test,
    "org.apache.beam" % "beam-runners-spark" % beamVersion, //needed in any case due to checkpoint dir config in EsbPipelineOptions
    "org.apache.beam" % "beam-runners-jet-experimental" % beamVersion, //needed in any case due to checkpoint dir config in EsbPipelineOptions
    "org.apache.beam" % "beam-runners-direct-java" % beamVersion % Test
  )
}
