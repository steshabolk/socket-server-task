package com.task.server.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class DbUtil {

    public static void closePreparedStatement(PreparedStatement... statements) {
        for (PreparedStatement st : statements) {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    log.warn("error closing prepared statement");
                }
            }
        }
    }

    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                log.warn("error closing db connection");
            }
        }
    }
}
