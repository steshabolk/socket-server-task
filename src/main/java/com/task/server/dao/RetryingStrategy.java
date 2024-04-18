package com.task.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RetryingStrategy {

    private static final String SERIALIZATION_ERROR_CODE = "40001";
    private static final String DEADLOCK_ERROR_CODE = "40P01";
    private static final int MAX_RETRIES = 10;

    public static <T> T execute(RetryableTransaction<T> transaction, Connection con, PreparedStatement... statements)
        throws SQLException {
        int retryCount = 0;
        while (true) {
            try {
                return transaction.run(con, statements);
            } catch (SQLException e) {
                if (SERIALIZATION_ERROR_CODE.equals(e.getSQLState()) || DEADLOCK_ERROR_CODE.equals(e.getSQLState())) {
                    con.rollback();
                    retryCount++;
                    if (retryCount > MAX_RETRIES) {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }
}
