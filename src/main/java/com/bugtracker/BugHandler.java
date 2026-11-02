package com.bugtracker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class BugHandler implements HttpHandler {

    public record CreateRequest(String title, String description, String severity) {}

    public record StatusRequest(String status) {}

    private final BugService service;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public BugHandler(BugService service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            route(ex);
        } catch (ValidationException e) {
            sendError(ex, 400, e.getMessage());
        } catch (NotFoundException e) {
            sendError(ex, 404, e.getMessage());
        } catch (JsonProcessingException e) {
            sendError(ex, 400, "invalid JSON body");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(ex, 500, "internal server error");
        }
    }

    private void route(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath().replaceAll("^/+|/+$", "");
        String[] parts = path.split("/");

        if (!parts[0].equals("bugs")) {
            throw new NotFoundException("route not found");
        }

        if (parts.length == 1) {
            if (method.equals("GET")) {
                sendJson(ex, 200, service.list());
            } else if (method.equals("POST")) {
                CreateRequest req = readBody(ex, CreateRequest.class);
                Bug bug = service.create(req.title(), req.description(), req.severity());
                sendJson(ex, 201, bug);
            } else {
                sendError(ex, 405, "method not allowed");
            }
        } else if (parts.length == 2) {
            if (method.equals("GET")) {
                sendJson(ex, 200, service.get(parts[1]));
            } else {
                sendError(ex, 405, "method not allowed");
            }
        } else if (parts.length == 3 && parts[2].equals("status")) {
            if (method.equals("PATCH")) {
                StatusRequest req = readBody(ex, StatusRequest.class);
                sendJson(ex, 200, service.updateStatus(parts[1], req.status()));
            } else {
                sendError(ex, 405, "method not allowed");
            }
        } else {
            throw new NotFoundException("route not found");
        }
    }

    private <T> T readBody(HttpExchange ex, Class<T> type) throws IOException {
        String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        T value = mapper.readValue(body, type);
        if (value == null) {
            throw new ValidationException("request body is required");
        }
        return value;
    }

    private void sendJson(HttpExchange ex, int status, Object payload) throws IOException {
        Main.send(ex, status, mapper.writeValueAsString(payload));
    }

    private void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, Map.of("error", message));
    }
}
