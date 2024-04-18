package com.task.server.dao;

import com.task.server.entity.User;
import com.task.server.exception.DbException;
import com.task.server.util.ConnectionManager;
import com.task.server.util.DbUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserDao {

    private static final String SAVE_USER = """
        INSERT INTO users(login, password) VALUES (?, ?)
        """;
    private static final String CREATE_BANK_ACCOUNT = """
        INSERT INTO bank_accounts(user_id) VALUES (?)
        """;
    private static final String EXISTS_BY_LOGIN = """
        SELECT count(id) AS count FROM users WHERE login = ?
        """;
    private static final String FIND_USER_BY_LOGIN = """
        SELECT id, login, password FROM users WHERE login = ?
        """;
    private final ConnectionManager connectionManager;

    public User save(User user) {
        try {
            return processSave(user)
                .orElseThrow(() -> new DbException("failed to save user: the user was not saved"));
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    public boolean existsByLogin(String login) {
        try (Connection con = connectionManager.get();
             PreparedStatement st = con.prepareStatement(EXISTS_BY_LOGIN)) {
            st.setString(1, login);
            ResultSet resultSet = st.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("count") > 0;
            }
            return false;
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    public Optional<User> findByLogin(String login) {
        try (Connection con = connectionManager.get();
             PreparedStatement st = con.prepareStatement(FIND_USER_BY_LOGIN)) {
            st.setString(1, login);
            ResultSet resultSet = st.executeQuery();
            User user = null;
            if (resultSet.next()) {
                user = buildUser(resultSet);
            }
            return Optional.ofNullable(user);
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    private User buildUser(ResultSet resultSet) throws SQLException {
        return User.builder()
            .id(resultSet.getLong("id"))
            .login(resultSet.getString("login"))
            .password(resultSet.getString("password"))
            .build();
    }

    private Optional<User> processSave(User user) throws SQLException {
        Connection con = null;
        PreparedStatement saveUserSt = null;
        PreparedStatement createBankAccountSt = null;
        try {
            con = connectionManager.get();
            saveUserSt = con.prepareStatement(SAVE_USER, Statement.RETURN_GENERATED_KEYS);
            createBankAccountSt = con.prepareStatement(CREATE_BANK_ACCOUNT);

            con.setAutoCommit(false);

            saveUserSt.setString(1, user.getLogin());
            saveUserSt.setString(2, user.getPassword());
            saveUserSt.executeUpdate();

            ResultSet resultSet = saveUserSt.getGeneratedKeys();
            if (resultSet.next()) {
                user.setId(resultSet.getLong("id"));

                createBankAccountSt.setLong(1, user.getId());
                createBankAccountSt.executeUpdate();

                con.commit();
                return Optional.of(user);
            } else {
                con.rollback();
                return Optional.empty();
            }
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                DbUtil.closeConnection(con);
            }
            DbUtil.closePreparedStatement(saveUserSt, createBankAccountSt);
        }
    }
}
