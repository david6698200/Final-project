package com.bugtracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BugApiTest {

    private static HttpServer server;
    private static String baseUrl;
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void startServer() throws Exception {
        BugService service = new BugService(new InMemoryBugRepository());
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/bugs", new BugHandler(service));
        server.start();
        baseUrl = "http://localhost:" + server.getAddress().getPort();
    }

    @AfterAll
    static void stopServer() {
        server.stop(0);
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path));
        if (body == null) {
            builder.method(method, BodyPublishers.noBody());
        } else {
            builder.method(method, BodyPublishers.ofString(body));
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String createBug() throws Exception {
        HttpResponse<String> response = send("POST", "/bugs",
                "{\"title\":\"Login fails\",\"description\":\"500\",\"severity\":\"HIGH\"}");
        assertEquals(201, response.statusCode());
        JsonNode json = mapper.readTree(response.body());
        assertNotNull(json.get("id"));
        return json.get("id").asText();
    }

    @Test
    void createBug_returns201() throws Exception {
        createBug();
    }

    @Test
    void createdBug_canBeReadBack() throws Exception {
        String id = createBug();

        HttpResponse<String> response = send("GET", "/bugs/" + id, null);

        assertEquals(200, response.statusCode());
        assertEquals("Login fails", mapper.readTree(response.body()).get("title").asText());
    }

    @Test
    void createWithMissingTitle_returns400() throws Exception {
        HttpResponse<String> response = send("POST", "/bugs",
                "{\"description\":\"no title\",\"severity\":\"HIGH\"}");
        assertEquals(400, response.statusCode());
    }

    @Test
    void createWithInvalidSeverity_returns400() throws Exception {
        HttpResponse<String> response = send("POST", "/bugs",
                "{\"title\":\"x\",\"severity\":\"BANANA\"}");
        assertEquals(400, response.statusCode());
    }

    @Test
    void createWithBrokenJson_returns400() throws Exception {
        HttpResponse<String> response = send("POST", "/bugs", "not json");
        assertEquals(400, response.statusCode());
    }

    @Test
    void getUnknownBug_returns404() throws Exception {
        HttpResponse<String> response = send("GET", "/bugs/does-not-exist", null);
        assertEquals(404, response.statusCode());
    }

    @Test
    void updateStatus_returns200AndChangesStatus() throws Exception {
        String id = createBug();

        HttpResponse<String> response = send("PATCH", "/bugs/" + id + "/status",
                "{\"status\":\"RESOLVED\"}");

        assertEquals(200, response.statusCode());
        assertEquals("RESOLVED", mapper.readTree(response.body()).get("status").asText());
    }

    @Test
    void updateStatusWithInvalidValue_returns400() throws Exception {
        String id = createBug();

        HttpResponse<String> response = send("PATCH", "/bugs/" + id + "/status",
                "{\"status\":\"BANANA\"}");

        assertEquals(400, response.statusCode());
    }

    @Test
    void updateStatusOfUnknownBug_returns404() throws Exception {
        HttpResponse<String> response = send("PATCH", "/bugs/nope/status",
                "{\"status\":\"RESOLVED\"}");
        assertEquals(404, response.statusCode());
    }
}
