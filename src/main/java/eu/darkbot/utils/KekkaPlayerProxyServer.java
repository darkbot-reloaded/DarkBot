package eu.darkbot.utils;

import com.github.manolo8.darkbot.core.api.GameAPI;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class KekkaPlayerProxyServer extends Thread {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

    private final GameAPI.Handler handler;
    private ServerSocket serverSocket;

    public KekkaPlayerProxyServer(GameAPI.Handler handler) {
        super("Kekka Proxy");
        this.handler = handler;
        this.serverSocket = initializeServerSocket();
    }

    private ServerSocket initializeServerSocket() {
        for (int port = 7777; port < 7877; port++) {
            try {
                ServerSocket socket = new ServerSocket(port);
                handler.setLocalProxy(port);
                System.out.println("Proxy created at port: " + port);
                return socket;
            } catch (BindException e) {
                System.out.println("Skipping port " + port + " for proxy: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize server socket", e);
            }
        }
        throw new IllegalStateException("Every port is taken!");
    }

    @Override
    public void run() {
        int id = 0;
        try {
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);
                THREAD_POOL.submit(new RequestHandler(socket, id++));
            }
        } catch (IOException e) {
            System.out.println("Failed to accept request: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        try {
            serverSocket.close();
            THREAD_POOL.shutdown();
            if (!THREAD_POOL.awaitTermination(60, TimeUnit.SECONDS)) {
                THREAD_POOL.shutdownNow();
                if (!THREAD_POOL.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Thread pool did not terminate");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error shutting down: " + e.getMessage());
        }
    }

    private static class RequestHandler implements Runnable {
        private final Socket socket;
        private final int id;

        RequestHandler(Socket socket, int id) {
            this.socket = socket;
            this.id = id;
        }

        @Override
        public void run() {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true)) {
                String request;
                while ((request = reader.readLine()) != null) {
                    // Handle the request
                    writer.println("Handled request " + id + ": " + request);
                }
            } catch (IOException e) {
                System.err.println("Request handling failed: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Failed to close socket: " + e.getMessage());
                }
            }
        }
    }
}
