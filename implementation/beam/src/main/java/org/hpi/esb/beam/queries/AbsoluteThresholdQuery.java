package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.schemas.transforms.Filter;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.hpi.esb.beam.schema.SensorRecord;
import org.hpi.esb.beam.schema.SensorRecordDeserializerSerializer;

public class AbsoluteThresholdQuery extends Query {

    static final long serialVersionUID = 1965L;

    private static final int ERROR_LIMIT = 14963;
    private static final int COLUMN_IDX = 2;

    @Override
    public PCollection<String> expand(PCollection<String> input) {

        return input.
                apply(SensorRecordDeserializerSerializer.NAME, new SensorRecordDeserializerSerializer()).
                setSchema(SensorRecord.schema,
                        o -> Row.withSchema(SensorRecord.schema).addValues(o.id, o.ts, o.value).build(),
                        r -> new SensorRecord(r.getInt32(0), r.getInt64(1), r.getString(2))
                ).
                apply(Filter.<SensorRecord>create().whereFieldName("value", (String value) ->
                        Integer.parseInt(value.split("\t")[COLUMN_IDX]) > ERROR_LIMIT)
                ).
                apply(ParDo.of(new SensorRecordDeserializerSerializer.SerializeSensorRecords()));
    }
}
