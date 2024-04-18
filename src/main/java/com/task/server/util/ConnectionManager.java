package com.task.server.util;

import com.task.server.exception.DbException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConnectionManager {
    @Value("${db.url}")
    private String url;
    @Value("${db.username}")
    private String username;
    @Value("${db.password}")
    private String password;
    @Value("${db.driver}")
    private String driver;
    @Value("${db.pool-size}")
    private int poolSize;
    private BlockingQueue<Connection> pool;
    private List<Connection> sourceConnections;

    @PostConstruct
    public void init() {
        initConnectionPool();
    }

    @PreDestroy
    public void close() {
        closeConnectionPool();
    }

    public Connection get() {
        try {
            return pool.take();
        } catch (InterruptedException e) {
            throw new DbException(e.getMessage());
        }
    }

    private void initConnectionPool() {
        pool = new ArrayBlockingQueue<>(poolSize);
        sourceConnections = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            Connection connection = open();
            Connection proxyConnection = (Connection) Proxy.newProxyInstance(
                ConnectionManager.class.getClassLoader(),
                new Class[] {Connection.class},
                (proxy, method, args) -> method.getName().equals("close")
                    ? pool.add((Connection) proxy)
                    : method.invoke(connection, args)
            );
            pool.add(proxyConnection);
            sourceConnections.add(connection);
        }
    }

    private void closeConnectionPool() {
        for (Connection s : sourceConnections) {
            try {
                s.close();
            } catch (SQLException e) {
                log.warn("error closing db connection");
            }
        }
    }

    private Connection open() {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            log.warn("error opening db connection");
            throw new DbException(e.getMessage());
        }
    }
}
