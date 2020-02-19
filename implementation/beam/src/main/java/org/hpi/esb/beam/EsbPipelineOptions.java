package org.hpi.esb.beam;

import org.apache.beam.runners.spark.SparkPipelineOptions;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptions;

public interface EsbPipelineOptions extends PipelineOptions {

    @Description("Specifies the addresses of the Jet cluster; needed only with external clusters")
    @Default.String("127.0.0.1:5701")
    String getJetServers();

    void setJetServers(String jetServers);

    @Description("Specifies where the fat-jar containing all the code is located; needed only with external clusters")
    String getCodeJarPathname();

    void setCodeJarPathname(String codeJarPathname);

    @Description("Local parallelism of Jet nodes")
    @Default.Integer(2)
    Integer getJetDefaultParallelism();

    void setJetDefaultParallelism(Integer localParallelism);

    @Default.InstanceFactory(value = SparkPipelineOptions.TmpCheckpointDirFactory.class)
    java.lang.String getCheckpointDir();

    void setCheckpointDir(java.lang.String checkpointDir);

    @Override
    @Default.InstanceFactory(JobNameFactory.class)
    String getJobName();

    @Override
    void setJobName(String jobName);

    @Description("Query to be executed")
    String getQuery();

    void setQuery(String value);

    @Description("Input topics separated by comma")
    String getInputTopics();

    void setInputTopics(String value);

    @Description("Output topic")
    String getOutputTopic();

    void setOutputTopic(String value);

}
