package de.pianoman911.indexcards;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.pianoman911.indexcards.config.ConfigLoader;
import de.pianoman911.indexcards.config.IndexCardsConfig;
import de.pianoman911.indexcards.logic.IndexCardsLogic;
import de.pianoman911.indexcards.main.IndexCardsConsole;
import de.pianoman911.indexcards.sql.DatabaseType;
import de.pianoman911.indexcards.sql.SqlManager;
import de.pianoman911.indexcards.sql.queue.QueueManager;
import de.pianoman911.indexcards.web.WebServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;

import java.nio.file.Path;
import java.sql.ResultSet;

public class IndexCards {

    public static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final Logger LOGGER = LogManager.getLogger(IndexCards.class);
    public static IndexCards INSTANCE;

    static {
        System.setProperty("java.util.logging.manager", org.apache.logging.log4j.jul.LogManager.class.getName());
        System.setProperty("java.awt.headless", "true");

        System.setOut(IoBuilder.forLogger(LOGGER).setLevel(Level.INFO).buildPrintStream());
        System.setErr(IoBuilder.forLogger(LOGGER).setLevel(Level.ERROR).buildPrintStream());
    }

    private final Path configPath;
    private boolean running = true;
    private IndexCardsConfig config;
    private SqlManager sql;
    private QueueManager queue;
    private IndexCardsLogic logic;

    public IndexCards(Path configPath) {
        INSTANCE = this;
        this.configPath = configPath;
    }

    public void shutdown(boolean clean) {
        this.running = false;

        LOGGER.info("Exiting, goodbye! \\(^O^)/");
        LogManager.shutdown();
        System.exit(clean ? 0 : 1);
    }

    public boolean isRunning() {
        return this.running;
    }

    public void start(long bootTime) {
        LOGGER.info("Starting IndexCards...");

        LOGGER.info("Reading configuration...");
        this.config = ConfigLoader.loadObject(this.configPath, IndexCardsConfig.class);

        LOGGER.info("Starting logic...");
        this.logic = new IndexCardsLogic(this);

        LOGGER.info("Starting queue...");
        this.queue = new QueueManager(this);

        LOGGER.info("Starting SQL...");
        this.sql = new SqlManager(this);

        LOGGER.info("Starting Webserver...");
        new WebServer(this).start();

        LOGGER.info("Starting console...");
        new IndexCardsConsole(this).startThread();

        LOGGER.info("Loading default hikari data sources...");
        this.sql.connectBasic().resolveCredentials();

        try (ResultSet result = this.queue.read(DatabaseType.USER, "SELECT COUNT(*) FROM users")) {
            result.first();
            LOGGER.info("UserCount: {}", result.getInt(1));
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try (ResultSet result = this.queue.read(DatabaseType.CARD, "SELECT COUNT(*) FROM cards")) {
            result.first();
            LOGGER.info("CardsCount: {}", result.getInt(1));
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        double startingTime = (System.currentTimeMillis() - bootTime) / 1000d;
        double startTime = Math.round(startingTime * 100d) / 100d;
        LOGGER.info("Service started in {}s, type \"help\" for help", startTime);
    }

    public IndexCardsConfig config() {
        return config;
    }

    public SqlManager sql() {
        return sql;
    }

    public QueueManager queue() {
        return queue;
    }

    public IndexCardsLogic logic() {
        return logic;
    }
}