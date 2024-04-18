package com.task.server.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface RetryableTransaction<T> {

    T run(Connection con, PreparedStatement... statements) throws SQLException;
}
