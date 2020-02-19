package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.schemas.transforms.Filter;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.Lists;
import org.hpi.esb.beam.db.DBManager;
import org.hpi.esb.beam.schema.SensorRecord;
import org.hpi.esb.beam.schema.SensorRecordDeserializerSerializer;

import java.sql.*;
import java.time.Instant;

public class MachinePowerQuery extends Query {

    static final long serialVersionUID = 1965L;

    private static final int ERROR_LIMIT = 8105;
    private static final int COLUMN_IDX = 4;

    private static Connection connection = new DBManager().getConnection();

    @Override
    public PCollection<String> expand(PCollection<String> input) {

        return input.
                apply(SensorRecordDeserializerSerializer.NAME, new SensorRecordDeserializerSerializer()).
                setSchema(SensorRecord.schema,
                        o -> Row.withSchema(SensorRecord.schema).addValues(o.id, o.ts, o.value).build(),
                        r -> new SensorRecord(r.getInt32(0), r.getInt64(1), r.getString(2))
                ).
                apply(Filter.<SensorRecord>create().whereFieldName("value",
                        (String value) -> Integer.parseInt(value.split("\t")[COLUMN_IDX]) < ERROR_LIMIT)).
                apply(Filter.<SensorRecord>create().whereFieldNames(Lists.newArrayList("ts", "value"),
                        record -> isNotScheduledDowntime(record.getInt64("ts"), record.getString("value"))
                )).
                apply(ParDo.of(new SensorRecordDeserializerSerializer.SerializeSensorRecords()));
    }

    private boolean isNotScheduledDowntime(long ts, String value) {
        String[] recordAsList = value.split("\t");
        int workplace = Integer.parseInt(recordAsList[recordAsList.length - 1]);

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT \"WP_DOWNTIME_START\", \"WP_DOWNTIME_END\" FROM workplace WHERE \"WP_ID\" = ?");
            preparedStatement.setInt(1, workplace);
            ResultSet rs = preparedStatement.executeQuery();
            rs.next();
            Timestamp dtStart = rs.getTimestamp(1);
            Timestamp dtEnd = rs.getTimestamp(2);
            Instant timestamp = Instant.ofEpochMilli(ts);
            return !timestamp.isAfter(dtStart.toInstant()) ||
                    !timestamp.isBefore(dtEnd.toInstant());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
            return false;
        }
    }
}
