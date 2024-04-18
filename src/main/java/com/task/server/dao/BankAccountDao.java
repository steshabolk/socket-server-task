package com.task.server.dao;

import com.task.server.exception.ApiException;
import com.task.server.exception.ApiExceptionType;
import com.task.server.exception.DbException;
import com.task.server.util.ConnectionManager;
import com.task.server.util.DbUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class BankAccountDao {

    private static final String UPDATE_BALANCE_BY_USER_LOGIN = """
        UPDATE bank_accounts
        SET balance = ?
        WHERE user_id = (SELECT u.id FROM users u WHERE u.login = ?)
        RETURNING balance
        """;
    private static final String GET_BALANCE_BY_USER_LOGIN = """
        SELECT ba.balance
        FROM bank_accounts ba
        JOIN users u ON ba.user_id = u.id
        WHERE u.login = ?
        """;
    private final ConnectionManager connectionManager;

    public Double getBalance(String login) {
        try (Connection con = connectionManager.get();
             PreparedStatement st = con.prepareStatement(GET_BALANCE_BY_USER_LOGIN)) {
            st.setString(1, login);
            ResultSet resultSet = st.executeQuery();
            return getBalance(resultSet, login);
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    public Double sendMoney(String fromLogin, String toLogin, Double amount) {
        try {
            return transferFunds(fromLogin, toLogin, amount);
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    private Double transferFunds(String fromLogin, String toLogin, Double amount) throws SQLException {
        Connection con = null;
        int iso = Connection.TRANSACTION_READ_COMMITTED;
        PreparedStatement getBalanceSt = null;
        PreparedStatement updateBalanceSt = null;
        try {
            con = connectionManager.get();
            iso = con.getTransactionIsolation();
            getBalanceSt = con.prepareStatement(GET_BALANCE_BY_USER_LOGIN);
            updateBalanceSt = con.prepareStatement(UPDATE_BALANCE_BY_USER_LOGIN);

            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            Double updatedBalance = RetryingStrategy.execute(
                transferFundsTransaction(fromLogin, toLogin, amount),
                con,
                getBalanceSt,
                updateBalanceSt
            );

            con.commit();
            return updatedBalance;
        } catch (SQLException | ApiException e) {
            if (con != null) {
                con.rollback();
            }
            throw e;
        } finally {
            if (con != null) {
                con.setTransactionIsolation(iso);
                con.setAutoCommit(true);
                DbUtil.closeConnection(con);
            }
            DbUtil.closePreparedStatement(getBalanceSt, updateBalanceSt);
        }
    }

    private RetryableTransaction<Double> transferFundsTransaction(String fromLogin, String toLogin, Double amount) {
        return new RetryableTransaction<Double>() {
            @Override
            public Double run(Connection con, PreparedStatement... statements) throws SQLException {
                PreparedStatement getBalanceSt = statements[0];
                PreparedStatement updateBalanceSt = statements[1];
                getBalanceSt.setString(1, fromLogin);
                Double fromBalance = getBalance(getBalanceSt.executeQuery(), fromLogin);
                if (fromBalance < amount) {
                    throw ApiExceptionType.INSUFFICIENT_FUNDS.toException(fromLogin);
                }
                getBalanceSt.setString(1, toLogin);
                Double toBalance = getBalance(getBalanceSt.executeQuery(), toLogin);

                updateBalanceSt.setDouble(1, toBalance + amount);
                updateBalanceSt.setString(2, toLogin);
                updateBalanceSt.executeQuery();

                updateBalanceSt.setDouble(1, fromBalance - amount);
                updateBalanceSt.setString(2, fromLogin);

                return getBalance(updateBalanceSt.executeQuery(), fromLogin);
            }
        };
    }

    private Double getBalance(ResultSet resultSet, String login) throws SQLException {
        if (resultSet.next()) {
            return resultSet.getDouble("balance");
        } else {
            throw ApiExceptionType.USER_NOT_FOUND.toException(login);
        }
    }
}
