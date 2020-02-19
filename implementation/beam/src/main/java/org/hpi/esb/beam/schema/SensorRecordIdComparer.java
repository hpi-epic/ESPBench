package org.hpi.esb.beam.schema;

import java.util.Comparator;

import scala.Tuple2;

public class SensorRecordIdComparer implements Comparator<Tuple2<SensorRecord, Double>> {

    @Override
    public int compare(Tuple2<SensorRecord, Double> o1, Tuple2<SensorRecord, Double> o2) {
        return Integer.compare(o1._1.id, o2._1.id);
    }
}
