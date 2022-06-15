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

public class KekkaPlayerProxyServer extends Thread {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

    private final GameAPI.Handler handler;
    private ServerSocket serverSocket;

    public KekkaPlayerProxyServer(GameAPI.Handler handler) {
        super("Kekka Proxy");
        this.handler = handler;
        for (int port = 7777; port < 7877; port++) {
            try {
                serverSocket = new ServerSocket(port);

                if (serverSocket.isBound()) {
                    handler.setLocalProxy(port);
                    System.out.println("Proxy created at port: " + port);
                    break;
                }
            }
             catch (BindException e) {
                 System.out.println("Skipping port " + port + " for proxy: " + e.getMessage());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (serverSocket == null)
            throw new IllegalStateException("Every port is taken!");
    }

    @Override
    public void run() {
        int id = 0;
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setTcpNoDelay(true);

                THREAD_POOL.submit(new RequestHandler(socket, id++));
            } catch (IOException e) {
                System.out.println("Failed to make a request: " + e.getMessage());
                e.printStackTrace();
            }
        }
        handler.setLocalProxy(0); // API needs to be refreshed
    }

    public static class RequestHandler implements Runnable {

        private final Socket proxyRequest;
        private final int id;

        public RequestHandler(Socket proxyRequest, int id) {
            this.proxyRequest = proxyRequest;
            this.id = id;
        }

        @Override
        public void run() {
            try (Socket proxySocket = proxyRequest;
                 BufferedReader br = new BufferedReader(new InputStreamReader(proxySocket.getInputStream()))) {

                String header = br.readLine();

                System.out.println("START: " + id + " | " + header);
                if (header.startsWith("CONNECT"))
                    handleConnect(header);
                else if (header.startsWith("GET"))
                    handleGet(header, br);

                System.out.println("COMPLETED: " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleConnect(String header) {
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proxyRequest.getOutputStream()))) {
                bw.write("HTTP/1.0 200 Connection Established"); //accept any
                bw.write("\r\n\r\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void handleGet(String header, BufferedReader proxyBr) throws IOException {
            String[] sp = header.split(" ");
            if (sp.length != 3) throw new RuntimeException();

            if (header.contains("http:") && header.contains("443"))
                header = header.replace("http:", "https:");

            URI uri = URI.create(sp[1]);

            try (OutputStream proxyOutput = proxyRequest.getOutputStream();
                 Socket socket = SSLSocketFactory.getDefault().createSocket(uri.getHost(), uri.getPort());
                 PrintWriter pw = new PrintWriter(socket.getOutputStream())) {

                socket.setTcpNoDelay(true);
                pw.println(header);

                String temp;
                while (!(temp = proxyBr.readLine()).isEmpty()) {
                    if (temp.contains("Proxy")) pw.println("Connection: close");
                    else pw.println(temp);
                }

                pw.print("\r\n");
                pw.flush();

                byte[] buffer = new byte[65536];

                int read;
                while ((read = socket.getInputStream().read(buffer)) != -1)
                    proxyOutput.write(buffer, 0, read);
            }
        }
    }
}
