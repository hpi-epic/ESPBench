package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;

import java.util.Arrays;

public class SensorRecordDeserializerSerializer extends PTransform<PCollection<String>, PCollection<SensorRecord>> {

    static final long serialVersionUID = 1965L;
    public static final String NAME = "SensorRecordDeserializer";

    static class DeserializeSensorRecords extends DoFn<String, SensorRecord> {
        static final long serialVersionUID = 1965L;

        @ProcessElement
        public void processElement(@Element String input, OutputReceiver<SensorRecord> out) {
            if (input != null) {
                String[] record = input.split(";");
                if (record.length < 3) {
                    System.err.println("Unsupported record format: " + Arrays.toString(record));
                    return;
                }
                out.output(new SensorRecord(Integer.parseInt(record[0]), Long.parseLong(record[1]), record[2]));
            }
        }
    }

    public static class SerializeSensorRecords extends DoFn<SensorRecord, String> {
        static final long serialVersionUID = 1965L;

        @ProcessElement
        public void processElement(@Element SensorRecord input, OutputReceiver<String> out) {
            if (input != null) {
                out.output(input.serialize());
            }
        }
    }

    @Override
    public PCollection<SensorRecord> expand(PCollection<String> input) {
        return input.apply(ParDo.of(new DeserializeSensorRecords()));
    }
}
