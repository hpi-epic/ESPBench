package org.hpi.esb.beam;

import org.hpi.esb.commons.config.Configs;
import org.hpi.esb.commons.util.ScalaToJavaConverter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class Main {

    public static void main(final String[] args) {
        int threadSleepInMillis = 1000;

        List<Configs.InternalQueryConfig> configList = ScalaToJavaConverter.list(Configs.benchmarkConfig().fullQueryConfigs());
        for (Configs.InternalQueryConfig queryConfig : configList) {
            try {
                Thread.sleep(threadSleepInMillis);
            } catch (Exception e) {
                e.printStackTrace();
            }
            runJob(queryConfig.queryName(),
                    ScalaToJavaConverter.list(queryConfig.inputTopics()),
                    queryConfig.outputTopic());
        }
    }

    private static void runJob(final String queryName, final List<String> inputTopicList, final String outputTopic) {
        String system = Config.get(Config.SYSTEM_PROPERTY);
        String command = getSystemExtension(system).getExecutionCommand(queryName, inputTopicList, outputTopic);

        executeCommand(command);
    }

    private static void executeCommand(String command) {
        try {
            System.out.println("Executing: " + command);

            Process proc = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", command});

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            // Read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // Read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Config.SystemConfig getSystemExtension(String system) {
        Config.SystemConfig config = null;
        switch (system) {
            case Config.SYSTEM.FLINK:
                config = new Config.FlinkSystemConfig();
                break;
            case Config.SYSTEM.SPARK:
                config = new Config.SparkSystemConfig();
                break;
            case Config.SYSTEM.HAZELCAST_JET:
                config = new Config.HazelcastJetSystemConfig();
                break;
            default:
                System.err.println("The configured runner is not supported unfortunately: " + system);
                System.exit(1);
        }
        return config;
    }

}