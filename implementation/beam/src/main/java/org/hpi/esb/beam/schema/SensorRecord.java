package org.hpi.esb.beam.schema;

import org.apache.beam.sdk.schemas.Schema;

import java.io.Serializable;

public class SensorRecord implements Serializable {

    static final long serialVersionUID = 1965L;

    public int id;
    public long ts;
    public String value;

    public static Schema schema = new Schema.Builder().addInt32Field("id").addInt64Field("ts").addStringField("value").build();

    public SensorRecord(int id, long ts, String value) {
        this.id = id;
        this.ts = ts;
        this.value = value;
    }

    public String serialize() {
        return String.format("%d;%s", id, value);
    }

    @Override
    public String toString() {
        return String.format("id: %d; ts: %d; value: %s", id, ts, value);
    }

}
