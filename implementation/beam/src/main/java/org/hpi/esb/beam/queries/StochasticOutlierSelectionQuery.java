package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.PCollection;
import org.hpi.esb.beam.schema.SensorRecord;
import org.hpi.esb.beam.schema.SensorRecordDeserializerSerializer;
import org.hpi.esb.beam.schema.StochasticOutlierSelectionAggregator;
import scala.Tuple2;

import java.util.List;

public class StochasticOutlierSelectionQuery extends Query {

    public static final int PERPLEXITY = 30;
    public static final int ITERATIONS = 50;
    public static final int COLUMN_INDEX_VALUE1 = 2;//mf01 - Electrical Power Main Phase 1
    public static final int COLUMN_INDEX_VALUE2 = 3;//mf02 - Electrical Power Main Phase 2

    private static final int MAX_WINDOW_COUNT = 500;
    public static final double OUTLIER_THRESHOLD = 0.5;

    static final long serialVersionUID = 1965L;

    @Override
    public PCollection<String> expand(PCollection<String> input) {

        return input.
                apply(SensorRecordDeserializerSerializer.NAME, new SensorRecordDeserializerSerializer())
                .apply(
                        Window.<SensorRecord>into(
                                new GlobalWindows()).
                                triggering(Repeatedly.forever(
                                        AfterFirst.of(
                                                AfterPane.elementCountAtLeast(MAX_WINDOW_COUNT),
                                                AfterPane.elementCountAtLeast(MAX_WINDOW_COUNT)
                                        )
                                        )
                                ).discardingFiredPanes()
                )
                .apply(Combine.globally(new StochasticOutlierSelectionAggregator()))
                .apply(ParDo.of(new ResultFlattener()));
    }


    static class ResultFlattener extends DoFn<List<Tuple2<SensorRecord, Double>>, String> {
        static final long serialVersionUID = 1965L;

        @ProcessElement
        public void processElement(@Element List<Tuple2<SensorRecord, Double>> input, OutputReceiver<String> out) {

            for (Tuple2<SensorRecord, Double> tuple : input) {
                String output = tuple._1.serialize() + "\t" + String.format("%.2f", tuple._2);
                out.output(output);
            }

        }
    }
}
