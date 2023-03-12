package de.pianoman911.indexcards.main;

import de.pianoman911.indexcards.IndexCards;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.io.IoBuilder;

public class IndexCardsMain {


    private static final Logger LOGGER = LogManager.getLogger(IndexCardsMain.class);

    static {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        System.setProperty("java.awt.headless", "true");

        System.setOut(IoBuilder.forLogger(LOGGER).setLevel(Level.INFO).buildPrintStream());
        System.setErr(IoBuilder.forLogger(LOGGER).setLevel(Level.ERROR).buildPrintStream());
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Thread.currentThread().setName("Updater Start Thread");

        LOGGER.info("Parsing arguments...");
        ArgumentParser arguments = new ArgumentParser(args);

        if (arguments.isDebug()) {
            Configurator.setRootLevel(Level.DEBUG);
            LOGGER.warn("Starting in debug mode...");
        } else {
            LOGGER.info("Starting service...");
        }

        new IndexCards(arguments.getConfigPath()).start(start);
    }
}
