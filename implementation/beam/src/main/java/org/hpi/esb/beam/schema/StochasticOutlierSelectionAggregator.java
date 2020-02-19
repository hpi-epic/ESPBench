package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.transforms.Combine;
import scala.Tuple2;

import java.io.Serializable;
import java.util.List;

@DefaultCoder(AvroCoder.class)
public class StochasticOutlierSelectionAggregator extends Combine.CombineFn<SensorRecord, StochasticOutlierSelectionAggregate, List<Tuple2<SensorRecord, Double>>> implements Serializable {

    static final long serialVersionUID = 1965L;

    @Override
    public StochasticOutlierSelectionAggregate createAccumulator() {
        return new StochasticOutlierSelectionAggregate();
    }

    @Override
    public StochasticOutlierSelectionAggregate addInput(StochasticOutlierSelectionAggregate mutableAccumulator, SensorRecord input) {
        mutableAccumulator.addValue(input);
        return mutableAccumulator;
    }

    @Override
    public StochasticOutlierSelectionAggregate mergeAccumulators(Iterable<StochasticOutlierSelectionAggregate> accumulators) {
        return null;
    }

    @Override
    public List<Tuple2<SensorRecord, Double>> extractOutput(StochasticOutlierSelectionAggregate accumulator) {
        return accumulator.getResults();
    }
}
