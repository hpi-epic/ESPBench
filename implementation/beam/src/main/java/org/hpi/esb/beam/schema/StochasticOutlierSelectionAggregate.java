package org.hpi.esb.beam.schema;

import com.github.gnni.outlierdetection.StochasticOutlierDetection;
import org.apache.beam.repackaged.core.org.apache.commons.lang3.tuple.Triple;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.Lists;
import org.hpi.esb.beam.queries.StochasticOutlierSelectionQuery;
import scala.Tuple2;

import java.util.Collections;
import java.util.List;

@DefaultCoder(AvroCoder.class)
public class StochasticOutlierSelectionAggregate {

    private List<Triple<Double, Double, SensorRecord>> valueList = Lists.newArrayList();

    public void addValue(SensorRecord record) {
        String[] recordAsArray = record.value.split("\t");
        valueList.add(Triple.of(
                Double.parseDouble(recordAsArray[StochasticOutlierSelectionQuery.COLUMN_INDEX_VALUE1]),
                Double.parseDouble(recordAsArray[StochasticOutlierSelectionQuery.COLUMN_INDEX_VALUE2]),
                record));
    }

    public List<Tuple2<SensorRecord, Double>> getResults() {
        double[][] convertedValueList = new double[valueList.size()][];
        for (int i = 0; i < valueList.size(); i++) {
            double[] valuePairArray = new double[2];
            valuePairArray[0] = valueList.get(i).getLeft();
            valuePairArray[1] = valueList.get(i).getMiddle();
            convertedValueList[i] = valuePairArray;
        }
        Tuple2<Object, Object>[] outlierProbs =
                StochasticOutlierDetection.performOutlierDetection(convertedValueList,
                        StochasticOutlierSelectionQuery.PERPLEXITY,
                        StochasticOutlierSelectionQuery.ITERATIONS
                );
        List<Tuple2<SensorRecord, Double>> resultList = Lists.newArrayList();
        for (int i = 0; i < outlierProbs.length; i++) {
            Tuple2<Object, Object> outlierProb = outlierProbs[i];
            if ((double) outlierProb._2 >= StochasticOutlierSelectionQuery.OUTLIER_THRESHOLD) {
                resultList.add(new Tuple2<SensorRecord, Double>(valueList.get(((Long) outlierProbs[i]._1).intValue()).getRight(), (double) outlierProbs[i]._2));
            }
        }
        Collections.sort(resultList, new SensorRecordIdComparer());
        valueList = Lists.newArrayList();
        return resultList;

    }

}
