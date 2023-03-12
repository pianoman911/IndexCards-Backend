package de.pianoman911.indexcards.web.api;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.logic.User;
import de.pianoman911.indexcards.util.StreamUtils;

import java.io.IOException;

public class CardDoneHandler implements HttpHandler {

    private final IndexCards service;

    public CardDoneHandler(IndexCards service) {
        this.service = service;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        User user;
        int id;
        long timestamp;

        try {
            JsonObject response = StreamUtils.readJsonFully(exchange.getRequestBody());
            user = service.logic().session(response.get("session").getAsString());

            if (user == null) {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
                return;
            }

            id = response.get("id").getAsInt();
            timestamp = response.get("timestamp").getAsLong();
        } catch (Exception e) {
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
            return;
        }

        service.logic().doneCard(user, id, timestamp);

        exchange.sendResponseHeaders(200, 0);
        exchange.getResponseBody().close();
    }
}
