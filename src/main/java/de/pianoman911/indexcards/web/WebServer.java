package de.pianoman911.indexcards.web;

import com.sun.net.httpserver.HttpServer;
import de.pianoman911.indexcards.IndexCards;
import de.pianoman911.indexcards.web.api.AccountCreateHandler;
import de.pianoman911.indexcards.web.api.AuthHandler;
import de.pianoman911.indexcards.web.api.CardDoneHandler;
import de.pianoman911.indexcards.web.api.CardNowHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer extends Thread {

    private final IndexCards service;

    public WebServer(IndexCards service) {
        super("WebServer");
        this.service = service;
    }

    @Override
    public void run() {
        System.out.println("Hello world!");

        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress("0.0.0.0", service.config().port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.createContext("/", new RootHandler());
        server.createContext("/api", new ApiHandler());
        server.createContext("/api/account/login", new AuthHandler(service));
        server.createContext("/api/account/create", new AccountCreateHandler(service));
        server.createContext("/api/cards/now", new CardNowHandler(service));
        server.createContext("/api/cards/done", new CardDoneHandler(service));

        server.start();

    }
}
