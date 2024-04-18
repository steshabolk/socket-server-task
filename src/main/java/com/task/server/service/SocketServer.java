package com.task.server.service;

import com.task.server.handler.ClientRequestHandler;
import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SocketServer {
    @Value("${server.port}")
    private int serverPort;
    @Value("${app.server.thread-pool-size}")
    private int threadPoolSize;
    @Value("${app.server.socket-timeout}")
    private int socketTimeout;
    private ExecutorService threadPool;
    private final ClientRequestHandler clientRequestHandler;

    @PostConstruct
    public void init() {
        threadPool = Executors.newFixedThreadPool(threadPoolSize);
        connectServer();
    }

    private void connectServer() {
        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            log.info("server is running on port {}", serverPort);
            while (true) {
                final Socket connectSocket = serverSocket.accept();
                connectSocket.setSoTimeout(socketTimeout);
                threadPool.submit(() -> processSocket(connectSocket));
            }
        } catch (IOException e) {
            log.warn("server error: {}", e.getMessage());
        }
    }

    private void processSocket(Socket connectSocket) {
        try (connectSocket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(connectSocket.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connectSocket.getOutputStream()))) {
            clientRequestHandler.handleClientRequest(reader, writer);
        } catch (Exception e) {
            log.warn("client request processing error: {}", e.getMessage());
        }
    }
}

