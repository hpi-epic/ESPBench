package org.hpi.esb.beam.db;

import org.hpi.esb.beam.Config;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBManager {

    static final long serialVersionUID = 1965L;

    private static final String JDBC_URL = Config.SystemConfig.JDBC_URL;
    private static final String DB_USER = Config.SystemConfig.DB_USER;
    private static final String DB_PASSWORD = Config.SystemConfig.DB_PASSWORD;

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL , DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
