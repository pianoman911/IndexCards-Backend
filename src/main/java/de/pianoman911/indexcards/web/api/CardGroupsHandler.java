package de.pianoman911.indexcards.web.api;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.util.StreamUtils;
import de.pianoman911.indexcards.web.WebServer;
import joptsimple.internal.Strings;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CardGroupsHandler implements HttpHandler {

    private final IndexCards service;

    public CardGroupsHandler(IndexCards service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (WebServer.checkCors(exchange)) {
            return;
        }
        try {
            Set<String> groups = service.logic().groups().get(5, TimeUnit.SECONDS);
            exchange.sendResponseHeaders(200, 0);
            JsonObject object = new JsonObject();
            object.addProperty("groups", Strings.join(groups, ">>>"));
            StreamUtils.writeJsonFully(object, exchange.getResponseBody());
            exchange.getResponseBody().close();
        } catch (Exception exception) {
            exchange.getPrincipal();
            exchange.sendResponseHeaders(401, 0);
            exchange.getResponseBody().close();
        }
    }
}
