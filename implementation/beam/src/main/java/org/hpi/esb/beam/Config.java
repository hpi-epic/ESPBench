package org.hpi.esb.beam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public final class Config {

    static final String SYSTEM_PROPERTY = "system";
    private static final String CONFIG_FILE_NAME = "beam.properties";
    private static Properties properties = new Properties();

    private static void loadConfig() {
        try (InputStream input = new FileInputStream(new File(".").getCanonicalPath() + "/implementation/beam/src/main/resources/" + CONFIG_FILE_NAME)) {
            properties.load(input);
        } catch (IOException ex) {
            try (InputStream input = new FileInputStream(new File(".").getCanonicalPath() + "/src/main/resources/" + CONFIG_FILE_NAME)) {
                properties.load(input);
            } catch (IOException ex2) {
                System.err.println("Sorry, unable to find " + CONFIG_FILE_NAME);
                ex.printStackTrace();
                ex2.printStackTrace();
                System.exit(1);
            }
        }
    }

    static String get(String propertyName) {
        if (properties.isEmpty()) {
            loadConfig();
        }
        String property = properties.getProperty(propertyName);
        if (property == null) {
            System.err.println("Couldn't find property '" + propertyName + "' in " + CONFIG_FILE_NAME);
            System.exit(1);
        }
        return property;
    }

    static final class SYSTEM {
        static final String FLINK = "flink";
        static final String SPARK = "spark";
        static final String HAZELCAST_JET = "hazelcastjet";
    }

    public abstract static class SystemConfig {
        static final String CLASS_PARAMETER = " --class=org.hpi.esb.beam.JobRunner ";
        static final String RUNNER_PARAMETER = " --runner=";
        static final String EXECUTABLE = " implementation/beam/target/Beam-assembly-0.1.0-SNAPSHOT.jar ";
        static final String PARALLELISM_PROPERTY = "parallelism";
        public static final String JDBC_URL = get("jdbc-url");
        public static final String DB_USER = get("db-user");
        public static final String DB_PASSWORD = get("db-password");
        public static final String KAFKA_BOOTSTRAP_SERVERS = get("kafka-bootstrap-servers");

        abstract String getExecutionCommand(String queryName, List<String> inputTopicList, String outputTopic);

        String getJobRunnerParameterString(String queryName, List<String> inputTopicList, String outputTopic) {
            queryName = queryName.trim();
            String inputTopicListString = inputTopicList.get(0).trim();
            Iterator<String> iter = inputTopicList.iterator();
            iter.next();
            while (iter.hasNext()) {
                inputTopicListString = inputTopicListString.trim() + "," + iter.next();
            }
            return " --query=" + queryName + " --inputTopics=" + inputTopicListString.trim() + " --outputTopic=" + outputTopic.trim() + " --jobName=" + queryName + " >output_" + queryName + ".log 2>&1 &";
        }

        String runner;
        String system;
    }

    static class SparkSystemConfig extends SystemConfig {

        private static final String SPARK_MASTER_PROPERTY = "spark-master";
        private static final String SPARK_CHECKPOINT_DIR_CONFIG = " --checkpointDir=\"hdfs://vm-hesse-bench01:9000/spark\" ";
        final String DEFAULT_SYSTEM_HOME = "/opt/spark";

        SparkSystemConfig() {
            runner = "SparkRunner ";
            system = Config.SYSTEM.SPARK;
        }

        @Override
        String getExecutionCommand(String queryName, List<String> inputTopicList, String outputTopic) {
            String systemHome = System.getenv(system.toUpperCase() + "_HOME");
            if (systemHome == null) {
                systemHome = DEFAULT_SYSTEM_HOME;
            }
            return "nohup " + systemHome + "/bin/spark-submit" +
                    " --executor-cores " + get(PARALLELISM_PROPERTY) +
                    " --master " + get(SPARK_MASTER_PROPERTY) +
                    CLASS_PARAMETER + EXECUTABLE + RUNNER_PARAMETER + runner + SPARK_CHECKPOINT_DIR_CONFIG +
                    getJobRunnerParameterString(queryName, inputTopicList, outputTopic);
        }
    }

    static class FlinkSystemConfig extends SystemConfig {

        final String DEFAULT_SYSTEM_HOME = "/opt/flink";

        FlinkSystemConfig() {
            runner = "FlinkRunner ";
            system = Config.SYSTEM.FLINK;
        }

        @Override
        String getExecutionCommand(String queryName, List<String> inputTopicList, String outputTopic) {
            String systemHome = System.getenv(system.toUpperCase() + "_HOME");
            if (systemHome == null) {
                systemHome = DEFAULT_SYSTEM_HOME;
            }
            return "nohup " + systemHome + "/bin/flink run -p " + get(PARALLELISM_PROPERTY) +
                    CLASS_PARAMETER + EXECUTABLE + RUNNER_PARAMETER + runner +
                    getJobRunnerParameterString(queryName, inputTopicList, outputTopic);
        }
    }

    static class HazelcastJetSystemConfig extends SystemConfig {

        private static final String JET_SERVERS_PROPERTY = "hazelcastjet-servers";

        HazelcastJetSystemConfig() {
            runner = "JetRunner ";
            system = SYSTEM.HAZELCAST_JET;
        }

        @Override
        String getExecutionCommand(String queryName, List<String> inputTopicList, String outputTopic) {
            return "nohup java -cp " + EXECUTABLE + " org.hpi.esb.beam.JobRunner --jetServers=" + get(JET_SERVERS_PROPERTY) + " --codeJarPathname=" +
                    EXECUTABLE.trim() + " --jetDefaultParallelism=" + get(PARALLELISM_PROPERTY) +
                    RUNNER_PARAMETER + runner +
                    getJobRunnerParameterString(queryName, inputTopicList, outputTopic);
        }
    }
}



