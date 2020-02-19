package org.hpi.esb.beam.queries;

import org.apache.beam.sdk.transforms.DoFn;
import org.hpi.esb.beam.db.DBManager;
import org.hpi.esb.beam.schema.SensorRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class ProcessingTimesQuery extends DoFn<SensorRecord, Void> {

    static final long serialVersionUID = 1965L;

    private static Connection connection = new DBManager().getConnection();

    @ProcessElement
    public void processElement(@Element SensorRecord input, OutputReceiver<Void> out) {
        try {
            PreparedStatement preparedStatementSetStart = connection.prepareStatement(
                    "UPDATE production_order_line SET \"POL_START_TS\" = current_timestamp WHERE \"POL_O_ID\" = ? AND \"POL_OL_NUMBER\" = ? AND \"POL_NUMBER\" = ?");
            PreparedStatement preparedStatementSetEnd = connection.prepareStatement(
                    "UPDATE production_order_line SET \"POL_END_TS\" = current_timestamp WHERE \"POL_O_ID\" = ? AND \"POL_OL_NUMBER\" = ? AND \"POL_NUMBER\" = ?");

            String[] recordAsArray = input.value.split("\t");
            int pol_o_id = Integer.parseInt(recordAsArray[0]);
            int pol_ol_number = Integer.parseInt(recordAsArray[1]);
            int pol_number = Integer.parseInt(recordAsArray[2]);

            if (Integer.parseInt(recordAsArray[recordAsArray.length - 1]) == 1) {
                preparedStatementSetStart.setInt(1, pol_o_id);
                preparedStatementSetStart.setInt(2, pol_ol_number);
                preparedStatementSetStart.setInt(3, pol_number);
                preparedStatementSetStart.executeUpdate();
            } else {
                preparedStatementSetEnd.setInt(1, pol_o_id);
                preparedStatementSetEnd.setInt(2, pol_ol_number);
                preparedStatementSetEnd.setInt(3, pol_number);
                preparedStatementSetEnd.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}
