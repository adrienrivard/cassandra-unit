package org.cassandraunit.cli;

import org.apache.commons.cli.*;
import org.cassandraunit.DataLoader;
import org.cassandraunit.LoadingOption;
import org.cassandraunit.dataset.FileDataSet;
import org.cassandraunit.model.StrategyModel;

public class CassandraUnitCommandLineLoader {

    private static CommandLineParser commandLineParser = null;

    private static Options options = null;

    private static CommandLine commandLine = null;

    private static boolean usageBeenPrinted = false;

    /**
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) {
        boolean exit = parseCommandLine(args);
        if (exit) {
            System.exit(1);
        } else {
            load();
        }

    }

    protected static boolean parseCommandLine(String[] args) {
        clearStaticAttributes();
        initOptions();
        commandLineParser = new PosixParser();
        boolean exit = false;
        try {
            commandLine = commandLineParser.parse(options, args);
            if (commandLine.getOptions().length == 0) {
                exit = true;
                printUsage();
            } else {
                if (containBadReplicationFactorArgumentValue()) {
                    printUsage("Bad argument value for option r");
                    exit = true;
                } else if (containBadStrategyArgumentValue()) {
                    printUsage("Bad argument value for option s");
                    exit = true;
                }
            }
        } catch (ParseException e) {
            printUsage(e.getMessage());
            exit = true;
        }

        return exit;

    }

    protected static void load() {
        System.out.println("Start Loading...");
        String host = commandLine.getOptionValue("h");
        String port = commandLine.getOptionValue("p");
        String file = commandLine.getOptionValue("f");
        LoadingOption loadingOption = new LoadingOption();
        loadingOption.setOnlySchema(commandLine.hasOption("o"));

        if (commandLine.hasOption("r")) {
            loadingOption.setReplicationFactor(Integer.parseInt(commandLine.getOptionValue("r")));
        }

        if (commandLine.hasOption("s")) {

            loadingOption.setStrategy(StrategyModel.fromValue(commandLine.getOptionValue("s")));
        }

        DataLoader dataLoader = new DataLoader("clusterToLoad", host + ":" + port);
        dataLoader.load(new FileDataSet(file), loadingOption);
        System.out.println("Loading completed");
    }

    private static boolean containBadReplicationFactorArgumentValue() {
        String replicationFactor = commandLine.getOptionValue("r");
        if (replicationFactor != null && !replicationFactor.trim().isEmpty()) {
            try {
                Integer.parseInt(replicationFactor);
                return false;
            } catch (NumberFormatException e) {
                return true;
            }
        }
        return false;
    }

    private static boolean containBadStrategyArgumentValue() {
        String strategy = commandLine.getOptionValue("s");
        if (strategy != null && !strategy.trim().isEmpty()) {
            try {
                StrategyModel.fromValue(strategy);
                return false;
            } catch (IllegalArgumentException e) {
                return true;
            }
        }
        return false;
    }

    private static void printUsage(String message) {
        System.out.println(message);
        printUsage();

    }

    private static void initOptions() {
        options = new Options();
        options.addOption(OptionBuilder.withLongOpt("file").hasArg().withDescription("dataset to load").isRequired()
                .create("f"));
        options.addOption(OptionBuilder.withLongOpt("host").hasArg().withDescription("target host (required)")
                .isRequired().create("h"));
        options.addOption(OptionBuilder.withLongOpt("port").hasArg().withDescription("target port (required)")
                .isRequired().create("p"));
        options.addOption(OptionBuilder.withLongOpt("onlySchema").withDescription("only load schema (optional)")
                .create("o"));
        options.addOption(OptionBuilder.withLongOpt("replicationFactor").hasArg()
                .withDescription("override the replication factor set in the dataset (optional)").create("r"));
        options.addOption(OptionBuilder.withLongOpt("strategy").hasArg()
                .withDescription("override the strategy set in the dataset (optional)").create("s"));

    }

    private static void clearStaticAttributes() {
        commandLine = null;
        commandLineParser = null;
        options = null;
        usageBeenPrinted = false;
    }

    private static void printUsage() {
        usageBeenPrinted = true;
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        formatter.printHelp("CassandraUnitLoader is a tool to load CassandraUnit data Set into cassandra cluster",
                options);
    }

    protected static CommandLine getCommandLine() {
        return commandLine;
    }

    protected static boolean isUsageBeenPrinted() {
        return usageBeenPrinted;
    }
}
