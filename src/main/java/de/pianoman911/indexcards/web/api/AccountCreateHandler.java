package de.pianoman911.indexcards.web.api;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.util.StreamUtils;
import de.pianoman911.indexcards.web.WebServer;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AccountCreateHandler implements HttpHandler {

    private final IndexCards service;

    public AccountCreateHandler(IndexCards service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (WebServer.checkCors(exchange)) {
            return;
        }
        String name;
        String password;

        try {
            JsonObject response = StreamUtils.readJsonFully(exchange.getRequestBody());
            name = response.get("name").getAsString();
            password = response.get("password").getAsString();
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
            return;
        }

        if (name == null || password == null || password.length() != 64 || name.length() > 50 || name.length() < 3) {
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
            return;
        }

        try {
            if (service.logic().createUser(name, password).get(5, TimeUnit.SECONDS) != null) {
                exchange.sendResponseHeaders(201, 0);
                exchange.getResponseBody().close();
            } else {
                exchange.sendResponseHeaders(400, 0);
                exchange.getResponseBody().close();
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
        }
    }
}
