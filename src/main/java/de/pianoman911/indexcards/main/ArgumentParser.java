package de.pianoman911.indexcards.main;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class ArgumentParser {

    private static final Logger LOGGER = LogManager.getLogger(ArgumentParser.class);
    private final boolean debug;

    private final Path configPath;
    public ArgumentParser(String[] args) {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        parser.acceptsAll(list("?", "h", "help"), "Print the help message");
        parser.acceptsAll(list("v", "ver", "version"), "Print the current version");
        parser.acceptsAll(list("d", "debug"), "Enables the debug mode (Warning: spam)");

        parser.acceptsAll(list("c", "cfg", "conf", "config"), "The main configuration file")
                .withRequiredArg().ofType(JoptPath.class)
                .defaultsTo(new JoptPath(Path.of("config.yml")))
                .describedAs("Yaml file");

        OptionSpec<String> nonOption = parser.nonOptions();
        OptionSet options = parser.parse(args);

        List<String> unrecognized = options.valuesOf(nonOption);
        if (unrecognized.size() > 0) LOGGER.warn("Unrecognized arguments: " + unrecognized);

        if (options.has("help")) {
            try {
                StringWriter writer = new StringWriter();
                parser.printHelpOn(writer);
                Arrays.stream(writer.toString().split("\n")).forEach(LOGGER::info);

                LogManager.shutdown();
                System.exit(0);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }

        if (options.has("version")) {
            String name = getClass().getPackage().getImplementationTitle();
            String version = getClass().getPackage().getImplementationVersion();
            String vendor = getClass().getPackage().getImplementationVendor();

            LOGGER.info("Running " + name + " v" + version + " by " + vendor);
            LogManager.shutdown();
            System.exit(0);
        }

        String currentPath = new File(".").getAbsolutePath();
        if (currentPath.contains("!") || currentPath.contains("+")) {
            LOGGER.error("Can't run with ! or + in the pathname.");
            LogManager.shutdown();
            System.exit(1);
        }

        debug = options.has("debug");
        configPath = ((JoptPath) options.valueOf("config")).getPath();
    }

    private static List<String> list(String... strings) {
        return Arrays.asList(strings);
    }

    public boolean isDebug() {
        return debug;
    }

    public Path getConfigPath() {
        return configPath;
    }

    public static class JoptPath {

        private final Path path;

        public JoptPath(Path path) {
            this.path = path;
        }

        @SuppressWarnings("unused") // Used via reflection by jopt
        public JoptPath(String path) {
            this.path = Path.of(path);
        }

        public Path getPath() {
            return path;
        }

        @Override
        public boolean equals(Object obj) {
            return path.equals(obj);
        }

        @Override
        public int hashCode() {
            return path.hashCode();
        }

        @Override
        public String toString() {
            return path.toString();
        }
    }
}

