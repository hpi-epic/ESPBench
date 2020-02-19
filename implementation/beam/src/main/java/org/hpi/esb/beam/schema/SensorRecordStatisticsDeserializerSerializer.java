package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.hpi.esb.beam.queries.StatisticsQuery;

import java.util.Arrays;

public class SensorRecordStatisticsDeserializerSerializer extends PTransform<PCollection<String>, PCollection<Long>> {

    static final long serialVersionUID = 1965L;
    public static final String NAME = "SensorRecordStatisticsDeserializer";

    static class DeserializeStatisticsSensorRecords extends DoFn<String, Long> {
        static final long serialVersionUID = 1965L;

        @ProcessElement
        public void processElement(@Element String input, OutputReceiver<Long> out) {
            if (input != null) {
                String[] record = input.split(";");
                if (record.length < 3) {
                    System.err.println("Unsupported record format: " + Arrays.toString(record));
                    return;
                }
                String[] values = record[2].split("\t");
                if (values.length < StatisticsQuery.COLUMN_INDEX + 1) {
                    System.err.println("Unsupported record format: " + Arrays.toString(record));
                    return;
                }
                out.output(Long.parseLong(values[StatisticsQuery.COLUMN_INDEX]));
            }
        }
    }

    @Override
    public PCollection<Long> expand(PCollection<String> input) {
        return input.apply(ParDo.of(new DeserializeStatisticsSensorRecords()));
    }
}
