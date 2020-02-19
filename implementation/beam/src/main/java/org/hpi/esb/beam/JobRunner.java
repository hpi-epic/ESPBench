package org.hpi.esb.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Values;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.ImmutableMap;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hpi.esb.beam.queries.*;
import org.hpi.esb.beam.schema.SensorRecord;
import org.hpi.esb.beam.schema.SensorRecordDeserializerSerializer;
import org.hpi.esb.commons.util.ScalaToJavaConverter;

public class JobRunner {

    private static final String BOOTSTRAP_SERVERS = Config.SystemConfig.KAFKA_BOOTSTRAP_SERVERS;

    public static void main(final String[] args) {
        PipelineOptionsFactory.register(EsbPipelineOptions.class);
        EsbPipelineOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(EsbPipelineOptions.class);

        Pipeline p = Pipeline.create(options);

        PCollection<String> input = p.apply(KafkaIO.<String, String>read()
                .withBootstrapServers(BOOTSTRAP_SERVERS)
                .withLogAppendTime()
                .withTopics(ScalaToJavaConverter.list(options.getInputTopics().split(",")))
                .withKeyDeserializer(StringDeserializer.class)
                .withValueDeserializer(StringDeserializer.class)
                .withConsumerConfigUpdates(ImmutableMap.<String, Object>of("auto.offset.reset", "earliest"))
                .withConsumerConfigUpdates(ImmutableMap.<String, Object>of("group.id", java.util.UUID.randomUUID().toString()))
                .withoutMetadata())
                .apply(Values.<String>create());

        boolean queryHasOutput = true;
        Query query = null;
        DoFn<SensorRecord, Void> noOutputQuery = null;
        switch (options.getQuery()) {
            case Query.IDENTITY_QUERY:
                query = new IdentityQuery();
                break;
            case Query.ABSOLUTE_THRESHOLD_QUERY:
                query = new AbsoluteThresholdQuery();
                break;
            case Query.MACHINE_POWER_QUERY:
                query = new MachinePowerQuery();
                break;
            //case Query.MULTITHRESHOLD_QUERY:
            case Query.PROCESSING_TIMES_QUERY:
                queryHasOutput = false;
                noOutputQuery = new ProcessingTimesQuery();
                break;
            case Query.STATISTICS_QUERY:
                query = new StatisticsQuery();
                break;
            case Query.STOCHASTIC_OUTLIER_SELECTION_QUERY:
                query = new StochasticOutlierSelectionQuery();
                break;
            default:
                System.err.println("Unsupported query: " + query);
                System.exit(1);
        }

        if (queryHasOutput) {
            KafkaIO.Write<Void, String> writeToKafka = KafkaIO.<Void, String>write()
                    .withBootstrapServers(BOOTSTRAP_SERVERS)
                    .withTopic(options.getOutputTopic())
                    .withValueSerializer(StringSerializer.class);

            input.apply(options.getQuery(), query)
                    .apply(writeToKafka.values());
        } else {
            input.apply(SensorRecordDeserializerSerializer.NAME, new SensorRecordDeserializerSerializer())
                    .apply(ParDo.of(noOutputQuery));
        }
        p.run().waitUntilFinish();

    }
}
