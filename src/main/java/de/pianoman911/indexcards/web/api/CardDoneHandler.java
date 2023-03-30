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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CardDoneHandler implements HttpHandler {

    private final IndexCards service;

    public CardDoneHandler(IndexCards service) {
        this.service = service;
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (WebServer.checkCors(exchange)) {
            return;
        }
        User user;
        int id;
        String input;

        try {
            JsonObject response = StreamUtils.readJsonFully(exchange.getRequestBody());
            user = service.logic().session(response.get("session").getAsString());
            input = response.get("input").getAsString();


            if (user == null || input == null) {
                exchange.sendResponseHeaders(401, 0);
                exchange.getResponseBody().close();
                return;
            }

            id = response.get("id").getAsInt();
        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
            return;
        }

        service.logic().refreshSession(user);

        try {

            IndexCard card = service.logic().card(id).get(5, TimeUnit.SECONDS);
            if (card == null) {
                exchange.sendResponseHeaders(204, 0);
                exchange.getResponseBody().close();
                return;
            }

            List<String> answers = new ArrayList<>(card.answers());
            JsonObject object = new JsonObject();
            if (containsIgnoreCase(answers, input)) {
                int status = service.logic().nextStatus(user, card).get(5, TimeUnit.SECONDS);
                long time = nextTimeStamp(status);
                service.logic().doneCard(user, id, time, status);
                object.addProperty("time", time);
                object.addProperty("correct", input);
                answers.removeIf(s -> s.equalsIgnoreCase(input));
                object.addProperty("others", String.join(">>>", answers));
            } else {
                service.logic().doneCard(user, id, nextTimeStamp(0), 0);
                object.addProperty("time", nextTimeStamp(0));
                answers.removeIf(s -> s.equalsIgnoreCase(input));
                object.addProperty("others", String.join(">>>", answers));
            }
            exchange.sendResponseHeaders(200, 0);
            StreamUtils.writeJsonFully(object, exchange.getResponseBody());
            exchange.getResponseBody().close();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
            return;
        }
    }

    private long nextTimeStamp(int status) {
        long now = System.currentTimeMillis() / 1000;
        return switch (status) {
            case 1 -> now + 60 * 60 * 24 * 2;
            case 2 -> now + 60 * 60 * 24 * 3;
            case 3 -> now + 60 * 60 * 24 * 7;
            case 4 -> now + 60 * 60 * 24 * 14;
            case 5 -> now + 60 * 60 * 24 * 30;
            default -> now + 60 * 60 * 24;
        };
    }

    private boolean containsIgnoreCase(List<String> list, String input) {
        for (String s : list) {
            if (s.equalsIgnoreCase(input)) {
                return true;
            }
        }
        return false;
    }
}
