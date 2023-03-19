package de.pianoman911.indexcards.web.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.logic.User;
import de.pianoman911.indexcards.util.StreamUtils;
import de.pianoman911.indexcards.web.WebServer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AuthHandler implements HttpHandler {

    private final IndexCards service;
    private final Gson gson = new Gson();

    public AuthHandler(IndexCards service) {
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
            System.out.println("name: " + name + " password: " + password);
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
            return;
        }

        if (name == null || password == null) {
            exchange.sendResponseHeaders(401, 0);
            exchange.getResponseBody().close();
            return;
        }

        try {
            User user = service.logic().handleAuth(name, password).get(5, TimeUnit.SECONDS);
            if (user == null) {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
                return;
            }
            exchange.sendResponseHeaders(200, 0);
            JsonObject json = new JsonObject();
            json.addProperty("session", user.session().toString());
            StreamUtils.writeJsonFully(json, exchange.getResponseBody());

            exchange.getResponseBody().close();

        } catch (Exception e) {
            exchange.sendResponseHeaders(401, 0);
            exchange.getResponseBody().close();
        }
    }
}
