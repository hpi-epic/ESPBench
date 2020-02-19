package org.hpi.esb.util

import kafka.admin.ReassignPartitionsCommand.Throttle
import kafka.admin.{AdminUtils, ReassignPartitionsCommand}
import kafka.utils.ZkUtils
import kafka.zk.KafkaZkClient
import net.liftweb.json.DefaultFormats
import net.liftweb.json.Extraction._
import net.liftweb.json.JsonAST._
import org.I0Itec.zkclient.{ZkClient, ZkConnection}
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.utils.Time
import org.hpi.esb.commons.config.Configs
import org.hpi.esb.commons.output.Tabulator
import org.hpi.esb.commons.util.Logging
import org.hpi.esb.util.Commands._
import scala.collection.JavaConverters._
import scala.collection.mutable

case class TopicManagementConfig(prefix: String = "", mode: String = "")

case class Reassignment(topic: String, partition: Int, replicas: List[Int])

case class TopicReassignments(version: Int, partitions: List[Reassignment])

object Commands {
  val CreateCommand = "create"
  val DeleteCommand = "delete"
  val ListCommand = "list"
  val HelpCommand = "help"
  val ReassignCommand = "reassign"
  val ReassignVerifyCommand = "reassignVerify"
}

object TopicManagement extends Logging {

  val isSecure = false
  val maxInFlightRequests = 10
  val time: Time = Time.SYSTEM
  val metricGroup = "myGroup"
  val metricType = "myType"
  lazy val zkClient: ZkClient = ZkUtils.createZkClient(ZookeeperServers, SessionTimeout, ConnectionTimeout)
  lazy val zkUtils = new ZkUtils(zkClient, new ZkConnection(ZookeeperServers), isSecure)
  lazy val kafkaZkClient = KafkaZkClient.apply(ZookeeperServers, isSecure, SessionTimeout, ConnectionTimeout, maxInFlightRequests, time, metricGroup, metricType)
  val ZookeeperServers: String = Configs.benchmarkConfig.zookeeperServers
  val BrokerList: String = Configs.benchmarkConfig.kafkaBootstrapServers
  val ZookeeperTopicPath = "/brokers/topics"
  val KafkaTopicPartitions = 1
  val KafkaTopicReplicationFactor = 1
  val SessionTimeout = 6000
  val ConnectionTimeout = 6000

  def main(args: Array[String]): Unit = {
    execute(getConfig(args))
  }

  def getConfig(args: Array[String]): TopicManagementConfig = {
    val parser = new scopt.OptionParser[TopicManagementConfig]("TopicManagement") {
      head("Topic Management for 'Enterprise Streaming Benchmark'")

      cmd(CreateCommand)
        .action((_, c) => c.copy(mode = CreateCommand))
        .text("All necessary benchmark topics get created.")
      cmd(DeleteCommand)
        .action((_, c) => c.copy(mode = DeleteCommand))
        .children(
          opt[String]('p', "prefix")
            .required()
            .action((x, c) => c.copy(prefix = x))
        )
        .text("All topics that match 'prefix' get deleted.")
      cmd(ListCommand)
        .action((_, c) => c.copy(mode = ListCommand))
        .children(
          opt[String]('p', "prefix")
            .action((x, c) => c.copy(prefix = x))
        )
        .text("All topics that match 'prefix' get listed.")
      cmd(ReassignCommand)
        .action((_, c) => c.copy(mode = ReassignCommand))
        .text("Redistributed all partitions equally among brokers.")

      cmd(ReassignVerifyCommand)
        .action((_, c) => c.copy(mode = ReassignVerifyCommand))
        .text("Verify success of partition redistribution.")

      help(HelpCommand)
    }

    parser.parse(args, TopicManagementConfig()) match {
      case Some(c) => c
      case _ => sys.exit(1)
    }
  }

  def execute(config: TopicManagementConfig): Unit = {
    config.mode match {
      case CreateCommand => createTopics()
      case ListCommand => listTopics(config.prefix)
      case DeleteCommand => deleteTopics(config.prefix)
      case ReassignCommand => reassignTopics()
      case ReassignVerifyCommand => reassignTopicsVerify()
      case _ => logger.info("Please use --help argument for usage.")
    }
  }

  def createTopics(): Unit = {
    val topics = Configs.benchmarkConfig.topics
    topics.foreach(topic =>
      try {
        AdminUtils.createTopic(zkUtils,
          topic,
          KafkaTopicPartitions,
          KafkaTopicReplicationFactor)
      }
      catch {
        case e: TopicExistsException => logger.info(s"Topic $topic already exists.")
      }
    )

    zkUtils.close()
  }

  def listTopics(prefix: String): Unit = {
    val topics = getMatchingTopics(getAllTopics.toBuffer, prefix)
    topics.foreach {
      topic =>
        logger.info(s"$topic")
    }
    kafkaZkClient.close()
  }

  def deleteTopics(prefix: String): Unit = {
    val topics = getMatchingTopics(getAllTopics.toBuffer, prefix)
    logger.info(s"The following topics are getting deleted from Zookeeper: $topics")
    topics.map(topic => {
      kafkaZkClient.deleteRecursive(ZkUtils.getTopicPath(topic))
    })
    kafkaZkClient.close()
  }

  def getMatchingTopics(topics: mutable.Buffer[String], prefix: String): mutable.Buffer[String] = {
    topics.filter(t => t.matches(s"$prefix.*"))
  }

  private def getAllTopics = {
    kafkaZkClient.getChildren(TopicManagement.ZookeeperTopicPath)
  }

  def reassignTopicsVerify(): Unit = {
    val topics = getSortedTopics
    val jsonStringLocal = createJsonString(topics, getNumberOfBrokers())
    ReassignPartitionsCommand.verifyAssignment(zkClient = kafkaZkClient,
      adminClientOpt = None,
      jsonString = jsonStringLocal)
  }

  def reassignTopics(): Unit = {
    val topics = getSortedTopics
    val jsonString = createJsonString(topics, getNumberOfBrokers())
    ReassignPartitionsCommand.executeAssignment(zkClient = kafkaZkClient,
      adminClientOpt = None,
      reassignmentJsonString = jsonString,
      throttle = Throttle(1000))
  }

  def getSortedTopics: List[String] = {
    val sourceTopics = Configs.benchmarkConfig.sourceTopics
    val identityTopics = Configs.benchmarkConfig.sinkTopics.filter(_.contains(Configs.QueryNames.IdentityQuery))
    val statisticsTopics = Configs.benchmarkConfig.sinkTopics.filter(_.contains(Configs.QueryNames.StatisticsQuery))
    val absoluteThresholdTopics = Configs.benchmarkConfig.sinkTopics.filter(_.contains(Configs.QueryNames.AbsoluteThresholdQuery))
    val machinePowerTopics = Configs.benchmarkConfig.sinkTopics.filter(_.contains(Configs.QueryNames.MachinePowerQuery))
    sourceTopics ++ identityTopics ++ statisticsTopics ++ absoluteThresholdTopics ++ machinePowerTopics
  }

  def createJsonString(topics: List[String], numberOfBrokers: Int): String = {

    val topicReassignmentList = topics.zipWithIndex.map { case (topic, index) => {
      val brokerId: Int = getBrokerId(index, numberOfBrokers)
      Reassignment(topic, partition = 0, replicas = List(brokerId))
    }
    }

    val topicReassignments = TopicReassignments(version = 1, partitions = topicReassignmentList)
    implicit val formats = DefaultFormats
    val jsonString = compactRender(decompose(topicReassignments))

    jsonString
  }

  def getBrokerId(index: Int, numberOfBrokers: Int): Int = {
    (index % numberOfBrokers)
  }


  def getNumberOfBrokers(): Int = {
    BrokerList.split(",").length
  }
}
