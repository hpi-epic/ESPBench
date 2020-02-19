package org.hpi.esb.beam.schema;


public class StatisticsAggregate {

    long min = Long.MAX_VALUE;
    long max = Long.MIN_VALUE;
    long sum = 0;
    long count = 0;
    private double avg = 0;
    private final String valueDividor = ",";

    StatisticsAggregate() {
    }

    StatisticsAggregate(String asString) {
        String[] valuesAsStringArray = asString.split(valueDividor);
        min = Long.parseLong(valuesAsStringArray[0]);
        max = Long.parseLong(valuesAsStringArray[1]);
        sum = Long.parseLong(valuesAsStringArray[2]);
        count = Long.parseLong(valuesAsStringArray[3]);
        avg = Long.parseLong(valuesAsStringArray[4]);
    }

    StatisticsAggregate(long min, long max, long sum, long count, double avg) {
        this.min = min;
        this.max = max;
        this.sum = sum;
        this.count = count;
        this.avg = avg;
    }

    void addValue(long value) {
        sum += value;
        count += 1;

        if (value < min) {
            min = value;
        }
        if (value > max) {
            max = value;
        }
        if (count > 0) {
            avg = (double) sum / count;
        }
    }

    @Override
    public String toString() {
        return avg + valueDividor + min + valueDividor + max + valueDividor + count;
    }
}
