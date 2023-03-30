package de.pianoman911.indexcards.web.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.web.WebServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ImageHandler implements HttpHandler {

    private static final HttpClient PROXY = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    private final IndexCards service;

    public ImageHandler(IndexCards service) {
        this.service = service;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (WebServer.checkCors(exchange)) {
            return;
        }
        try {
            String[] path = exchange.getRequestURI().getPath().split("/");
            String id = path[path.length - 1];
            String realUrl = service.logic().images().get(id);
            if (realUrl == null) {
                exchange.sendResponseHeaders(404, 0);
                exchange.getResponseBody().close();
            } else {
                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder();
                reqBuilder.uri(new URI(realUrl));
                reqBuilder.header("Accept", "image/png");
                reqBuilder.GET();
                exchange.getResponseHeaders().add("Content-Type", "image/png");
                try (InputStream is = PROXY.send(reqBuilder.build(), HttpResponse.BodyHandlers.ofInputStream()).body()) {
                    exchange.sendResponseHeaders(200, 0);
                    is.transferTo(exchange.getResponseBody());
                } catch (Exception e) {
                    exchange.sendResponseHeaders(404, 0);
                }
                exchange.getResponseBody().close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(400, 0);
            exchange.getResponseBody().close();
        }
    }
}
