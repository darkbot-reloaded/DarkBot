package eu.darkbot.utils;

import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.utils.NetworkUtils;

import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KekkaPlayerProxyServer extends Thread {

    private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);
    private static final ExecutorService RELAY_POOL = Executors.newCachedThreadPool();

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
                if (header == null) return;

                if (NetworkUtils.isProxyEnabled()) {
                    routeViaExternalProxy(header, br, proxySocket);
                } else if (header.startsWith("CONNECT")) {
                    handleConnect(header);
                } else if (header.startsWith("GET")) {
                    handleGet(header, br);
                }

                System.out.println("COMPLETED: " + id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void routeViaExternalProxy(String header, BufferedReader br, Socket proxySocket) throws IOException {
            List<String> head = new ArrayList<>();
            head.add(header);
            String line;
            while ((line = br.readLine()) != null && !line.isEmpty()) head.add(line);

            if (NetworkUtils.getProxyType() == Proxy.Type.SOCKS) routeViaSocksProxy(header, head, proxySocket);
            else routeViaHttpProxy(head, proxySocket);
        }

        /**
         * Chains the request through an external HTTP proxy, injecting proxy authentication if needed.
         * Works for both plain {@code GET} (absolute-form URL) and {@code CONNECT} tunnels.
         */
        private void routeViaHttpProxy(List<String> head, Socket proxySocket) throws IOException {
            try (Socket upstream = new Socket(NetworkUtils.getProxyHost(), NetworkUtils.getProxyPort())) {

                upstream.setTcpNoDelay(true);
                OutputStream upstreamOutput = upstream.getOutputStream();
                for (String headLine : head)
                    upstreamOutput.write((headLine + "\r\n").getBytes(StandardCharsets.UTF_8));

                if (!NetworkUtils.getProxyUser().isEmpty()) {
                    String auth = Base64.getEncoder().encodeToString(
                            (NetworkUtils.getProxyUser() + ":" + NetworkUtils.getProxyPassword())
                                    .getBytes(StandardCharsets.UTF_8));
                    upstreamOutput.write(("Proxy-Authorization: Basic " + auth + "\r\n")
                            .getBytes(StandardCharsets.UTF_8));
                }

                upstreamOutput.write("\r\n".getBytes(StandardCharsets.UTF_8));
                upstreamOutput.flush();

                relay(proxySocket, upstream);
            }
        }

        /**
         * Routes the request through an external SOCKS proxy using Java's {@link java.net.Socket} support.
         */
        private void routeViaSocksProxy(String header, List<String> head, Socket proxySocket) throws IOException {
            SocketAddress target = parseTarget(header, head);
            if (target == null) return;

            Socket upstream = new Socket(NetworkUtils.getProxy());
            try (upstream) {
                upstream.setTcpNoDelay(true);
                upstream.connect(target);

                OutputStream proxyOutput = proxySocket.getOutputStream();
                if (header.startsWith("CONNECT")) {
                    proxyOutput.write("HTTP/1.0 200 Connection Established\r\n\r\n".getBytes(StandardCharsets.UTF_8));
                    proxyOutput.flush();
                } else {
                    OutputStream upstreamOutput = upstream.getOutputStream();
                    for (String headLine : rewriteOriginForm(head))
                        upstreamOutput.write((headLine + "\r\n").getBytes(StandardCharsets.UTF_8));
                    upstreamOutput.write("\r\n".getBytes(StandardCharsets.UTF_8));
                    upstreamOutput.flush();
                }

                relay(proxySocket, upstream);
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

        private SocketAddress parseTarget(String header, List<String> head) {
            String[] sp = header.split(" ");
            if (sp.length < 2) return null;

            if (header.startsWith("CONNECT")) {
                int colon = sp[1].lastIndexOf(':');
                if (colon <= 0) return null;
                try {
                    return new InetSocketAddress(sp[1].substring(0, colon),
                            Integer.parseInt(sp[1].substring(colon + 1)));
                } catch (NumberFormatException e) {
                    return null;
                }
            }

            String target = sp[1];
            if (target.startsWith("http://") || target.startsWith("https://")) {
                URI uri = URI.create(target);
                int port = uri.getPort();
                if (port < 0) port = "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
                if (uri.getHost() == null) return null;
                return new InetSocketAddress(uri.getHost(), port);
            }

            for (String line : head) {
                if (line.regionMatches(true, 0, "Host:", 0, 5)) {
                    String host = line.substring(5).trim();
                    int colon = host.lastIndexOf(':');
                    if (colon > 0 && host.indexOf(':') == colon) {
                        try {
                            return new InetSocketAddress(host.substring(0, colon),
                                    Integer.parseInt(host.substring(colon + 1)));
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    }
                    return new InetSocketAddress(host, 80);
                }
            }
            return null;
        }

        private List<String> rewriteOriginForm(List<String> head) {
            String first = head.get(0);
            String[] sp = first.split(" ");
            if (sp.length < 2 ||
                    !(sp[1].startsWith("http://") || sp[1].startsWith("https://"))) return head;

            URI uri = URI.create(sp[1]);
            String path = uri.getRawPath() == null || uri.getRawPath().isEmpty() ? "/" : uri.getRawPath();
            if (uri.getRawQuery() != null) path += "?" + uri.getRawQuery();

            List<String> out = new ArrayList<>(head);
            out.set(0, sp[0] + " " + path + " " + (sp.length > 2 ? sp[2] : "HTTP/1.1"));
            return out;
        }

        /**
         * Relays bytes in both directions between the game and the upstream proxy/target,
         * closing both sockets once either side is done.
         */
        private static void relay(Socket client, Socket upstream) throws IOException {
            CountDownLatch latch = new CountDownLatch(2);
            copyAsync(client.getInputStream(), upstream.getOutputStream(), upstream, latch);
            copyAsync(upstream.getInputStream(), client.getOutputStream(), client, latch);
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            closeQuietly(client);
            closeQuietly(upstream);
        }

        private static void copyAsync(InputStream in, OutputStream out, Socket owner, CountDownLatch latch) {
            RELAY_POOL.submit(() -> {
                try {
                    in.transferTo(out);
                } catch (IOException e) {
                    // connection closed, relay ends
                } finally {
                    latch.countDown();
                    closeQuietly(owner);
                }
            });
        }

        private static void closeQuietly(Socket socket) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
