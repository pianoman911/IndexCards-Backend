package de.pianoman911.indexcards.main;

import de.pianoman911.indexcards.IndexCards;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class IndexCardsConsole extends SimpleTerminalConsole {

    private static final Logger LOGGER = LogManager.getLogger(IndexCardsConsole.class);
    private static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private final IndexCards service;

    public IndexCardsConsole(IndexCards service) {
        this.service = service;
    }

    public void startThread() {
        new Thread(this::start, "IndexCards Console Thread").start();
    }

    @Override
    protected boolean isRunning() {
        return this.service.isRunning();
    }

    @Override
    protected void runCommand(String rawCommand) {
        EXECUTOR.execute(() -> {
            try {
                String command = rawCommand.trim(), label;
                int firstSpaceIndex = command.indexOf(' ');

                if (firstSpaceIndex < 0) {
                    label = command.toLowerCase(Locale.ROOT);
                    command = "";
                } else {
                    label = command.substring(0, firstSpaceIndex).toLowerCase(Locale.ROOT);
                    command = command.substring(firstSpaceIndex + 1);
                }

                switch (label) {
                    case "stop" -> {
                        LOGGER.info("Shutting down...");
                        this.service.shutdown(true);
                    }
                    case "version", "about" -> {
                        String name = getClass().getPackage().getImplementationTitle();
                        String version = getClass().getPackage().getImplementationVersion();
                        String vendor = getClass().getPackage().getImplementationVendor();
                        LOGGER.info("Running {} v{} by {}", name, version, vendor);
                    }
                    case "help" -> {
                        LOGGER.info("Available commands:");
                        LOGGER.info(" - version (Displays version information)");
                        LOGGER.info(" - help (Shows this message)");
                        LOGGER.info(" - stop (Stops the service)");
                    }
                    default -> LOGGER.info("Unknown command. Type \"help\" for help.");
                }
            } catch (Throwable throwable) {
                LOGGER.error("An error occurred while executing command '{}'", rawCommand, throwable);
            }
        });
    }

    @Override
    protected void shutdown() {
        LOGGER.info("Received termination signal, shutting down...");
        this.service.shutdown(true);
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder.appName("IndexCards")
                .variable(LineReader.HISTORY_FILE, Paths.get(".console_history")));
    }
}
