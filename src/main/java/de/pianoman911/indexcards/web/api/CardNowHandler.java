package de.pianoman911.indexcards.web.api;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.logic.IndexCard;
import de.pianoman911.indexcards.logic.User;
import de.pianoman911.indexcards.util.StreamUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class CardNowHandler implements HttpHandler {

    private final IndexCards service;

    public CardNowHandler(IndexCards service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            JsonObject response = StreamUtils.readJsonFully(exchange.getRequestBody());
            User user = service.logic().session(response.get("session").getAsString());

            if (user == null) {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
                return;
            }


            IndexCard card = service.logic().nextCard(user).get(5, TimeUnit.SECONDS);
            if (card == null) {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
                return;
            }
            JsonObject object = new JsonObject();
            object.addProperty("id", card.id());
            object.addProperty("question", card.question());
            object.addProperty("answer", card.answer());

            exchange.sendResponseHeaders(200, 0);
            StreamUtils.writeJsonFully(object, exchange.getResponseBody());
            exchange.getResponseBody().close();

        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(401, 0);
            exchange.getResponseBody().close();
        }
    }
}
