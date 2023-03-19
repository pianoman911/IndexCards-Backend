package de.pianoman911.indexcards.web.api;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.logic.IndexCard;
import de.pianoman911.indexcards.logic.User;
import de.pianoman911.indexcards.util.StreamUtils;
import de.pianoman911.indexcards.web.WebServer;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CardNowHandler implements HttpHandler {

    private final IndexCards service;

    public CardNowHandler(IndexCards service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (WebServer.checkCors(exchange)) {
            return;
        }

        try {
            JsonObject response = StreamUtils.readJsonFully(exchange.getRequestBody());
            String session = response.get("session").getAsString();
            String group = null;
            if (response.has("group")) {
                group = response.get("group").getAsString();
            }

            User user = service.logic().session(session);

            if (user == null) {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
                return;
            }


            IndexCard card = service.logic().nextCard(user, group).get(5, TimeUnit.SECONDS);
            if (card == null) {
                exchange.sendResponseHeaders(204, 0);
                exchange.getResponseBody().close();
                return;
            }
            JsonObject object = new JsonObject();
            object.addProperty("id", card.id());
            object.addProperty("question", card.question());

            exchange.sendResponseHeaders(200, 0);
            StreamUtils.writeJsonFully(object, exchange.getResponseBody());
            exchange.getResponseBody().close();

        } catch (Exception e) {
            exchange.sendResponseHeaders(401, 0);
            exchange.getResponseBody().close();
        }
    }
}
