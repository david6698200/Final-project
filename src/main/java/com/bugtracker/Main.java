package com.bugtracker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

        BugRepository repository =
                new DynamoBugRepository(AwsConfig.dynamoDb(), AwsConfig.BUGS_TABLE);
        BugEventPublisher publisher =
                new SqsBugEventPublisher(AwsConfig.sqs(), AwsConfig.QUEUE_NAME);
        BugService service = new BugService(repository, publisher);

        Thread worker = new Thread(
                new BugEventWorker(AwsConfig.sqs(), AwsConfig.QUEUE_NAME, service),
                "bug-event-worker");
        worker.setDaemon(true);
        worker.start();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", Main::handleHealth);
        server.createContext("/bugs", new BugHandler(service));
        server.start();

        System.out.println("Bug tracker listening on port " + port
                + " (AWS endpoint: " + AwsConfig.ENDPOINT + ")");
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"method not allowed\"}");
            return;
        }
        send(exchange, 200, "{\"status\":\"ok\"}");
    }

    static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
