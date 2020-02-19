package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.PCollection;
import org.hpi.esb.beam.schema.SensorRecordStatisticsDeserializerSerializer;
import org.hpi.esb.beam.schema.StatisticsConcatCombine;
import org.joda.time.Duration;

public class StatisticsQuery extends Query {

    public static final int COLUMN_INDEX = 2;
    private static final long WINDOW_SIZE_IN_MILLIS = 1000;

    static final long serialVersionUID = 1965L;

    @Override
    public PCollection<String> expand(PCollection<String> input) {

        return input.
                apply(SensorRecordStatisticsDeserializerSerializer.NAME, new SensorRecordStatisticsDeserializerSerializer())
                .apply(
                        Window.<Long>into(
                                FixedWindows.of(Duration.millis(WINDOW_SIZE_IN_MILLIS)))
                                .discardingFiredPanes())
                .apply(Combine.globally(new StatisticsConcatCombine()).withoutDefaults());
    }
}
