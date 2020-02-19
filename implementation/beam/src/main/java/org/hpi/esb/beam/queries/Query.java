package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionList;
import org.hpi.esb.beam.schema.SensorRecord;

public abstract class Query extends PTransform<PCollection<String>, PCollection<String>> {

    static final long serialVersionUID = 1965L;
    public static final String IDENTITY_QUERY = "Identity";
    public static final String STATISTICS_QUERY = "Statistics";
    public static final String ABSOLUTE_THRESHOLD_QUERY = "AbsoluteThreshold";
    public static final String MACHINE_POWER_QUERY = "MachinePower";
    public static final String PROCESSING_TIMES_QUERY = "ProcessingTimes";
    public static final String STOCHASTIC_OUTLIER_SELECTION_QUERY = "StochasticOutlierSelection";

}
