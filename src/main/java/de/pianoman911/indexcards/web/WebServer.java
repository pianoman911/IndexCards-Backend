package de.pianoman911.indexcards.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.web.api.AccountCreateHandler;
import de.pianoman911.indexcards.web.api.AuthHandler;
import de.pianoman911.indexcards.web.api.CardDoneHandler;
import de.pianoman911.indexcards.web.api.CardGroupsHandler;
import de.pianoman911.indexcards.web.api.CardNowHandler;
import de.pianoman911.indexcards.web.api.ImageHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer extends Thread {

    private final IndexCards service;

    public WebServer(IndexCards service) {
        super("WebServer");
        this.service = service;
    }

    public static boolean checkCors(HttpExchange exchange) {
        try {
            // exchange.getResponseHeaders().add("Access-Control-Allow-Origin", IndexCards.INSTANCE.config().origin);
            if (exchange.getRequestMethod().equals("OPTIONS")) {
//                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "*");
//                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void run() {
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", service.config().port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //   server.createContext("/", new RootHandler());
        server.createContext("/api", new ApiHandler());
        server.createContext("/api/account/login", new AuthHandler(service));
        server.createContext("/api/account/create", new AccountCreateHandler(service));
        server.createContext("/api/cards/now", new CardNowHandler(service));
        server.createContext("/api/cards/done", new CardDoneHandler(service));
        server.createContext("/api/cards/groups", new CardGroupsHandler(service));
        server.createContext("/api/image", new ImageHandler(service));

        server.start();

    }
}
