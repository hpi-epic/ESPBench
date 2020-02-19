package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.transforms.Combine;

import java.util.Iterator;

public class StatisticsAggregator extends Combine.CombineFn<Long, StatisticsAggregator, String> implements Combine.AccumulatingCombineFn.Accumulator<Long, StatisticsAggregator, String> {

    static final long serialVersionUID = 1965L;

    StatisticsAggregate aggregate = new StatisticsAggregate();

    @Override
    public StatisticsAggregator createAccumulator() {
        return new StatisticsAggregator();
    }

    @Override
    public StatisticsAggregator addInput(StatisticsAggregator mutableAccumulator, Long input) {
        mutableAccumulator.aggregate.addValue(input);
        aggregate = mutableAccumulator.aggregate;
        return this;
    }

    @Override
    public StatisticsAggregator mergeAccumulators(Iterable<StatisticsAggregator> accumulators) {
        Iterator<StatisticsAggregator> iter = accumulators.iterator();
        if (iter.hasNext()) {
            StatisticsAggregator first = iter.next();
            if (!iter.hasNext()) {
                return first;
            } else {
                StatisticsAggregator second = iter.next();
                aggregate = mergeTwoAggregates(first.aggregate, second.aggregate);
            }
        }
        return this;
    }

    @Override
    public String extractOutput(StatisticsAggregator accumulator) {
        return accumulator.aggregate.toString();
    }

    @Override
    public void addInput(Long input) {
        aggregate.addValue(input);
    }

    @Override
    public void mergeAccumulator(StatisticsAggregator other) {
        aggregate = mergeTwoAggregates(aggregate, other.aggregate);
    }

    @Override
    public String extractOutput() {
        return aggregate.toString();
    }

    private StatisticsAggregate mergeTwoAggregates(StatisticsAggregate a, StatisticsAggregate b) {
        long mergedMin = a.min;
        if (b.min < a.min && b.min > Long.MIN_VALUE) {
            mergedMin = b.min;
        }

        long mergedMax = a.max;
        if (b.max > a.max && b.max < Long.MAX_VALUE) {
            mergedMax = b.max;
        }
        long mergedSum = a.sum + b.sum;
        long mergedCount = a.count + b.count;
        double mergedAvg = ((double) mergedSum) / mergedCount;

        return new StatisticsAggregate(mergedMin,
                mergedMax,
                mergedSum,
                mergedCount,
                mergedAvg);
    }
}
