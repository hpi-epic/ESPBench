package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.transforms.Combine;

public class StatisticsConcatCombine extends Combine.AccumulatingCombineFn<Long, StatisticsAggregator, String> {

    static final long serialVersionUID = 1965L;

    @Override
    public StatisticsAggregator createAccumulator() {
        return new StatisticsAggregator();
    }

    @Override
    public Coder<StatisticsAggregator> getAccumulatorCoder(CoderRegistry registry, Coder<Long> inputCoder) {
        return new StatisticsAggregatorCoder();
    }
}
