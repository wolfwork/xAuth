/*
 * xAuth for Bukkit
 * Copyright (C) 2012 Lycano <https://github.com/lycano/xAuth/>
 *
 * Copyright (C) 2011 CypherX <https://github.com/CypherX/xAuth/>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.luricos.bukkit.xAuth.utils;

import de.luricos.bukkit.xAuth.xAuth;
import org.apache.logging.log4j.core.filter.RegexFilter;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class xAuthLog {
    private static final Logger logger = Bukkit.getServer().getLogger();
    private static Level logLevel;
    private static Level defaultLevel = Level.INFO;

    private static List<xAuthLogFeatures> logFeatures = new ArrayList<xAuthLogFeatures>();
    private static List<String> commandsFilterList = new ArrayList<String>();
    private static List<String> commandsFilterExcludeList = new ArrayList<String>(Arrays.asList("quit", "q", "logout"));
    private static RegexFilter commandFilter;

    private static Filter logFilter;

    public enum xAuthLogFeatures {
        NONE,
        FILTER_COMMANDS
    }

    private static String loggerName = "xAuth";

    public static void initLogger() {
        setLevel(defaultLevel);
        enableFeature(xAuthLogFeatures.NONE);
    }

    /**
     * Check if a certain feature is enabled
     *
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     * @return true if enabled
     */
    public static boolean isFeatureEnabled(final xAuthLogFeatures feature) {
        return logFeatures.contains(feature);
    }

    /**
     * Enable Log Feature
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     */
    public static void enableFeature(final xAuthLogFeatures feature) {
        //info("Enable log feature: " + feature.toString());

        setFeature(feature);

        switch (feature) {
            case FILTER_COMMANDS:
                setFilterCommands();
                activateCommandFilter();
                break;
        }
    }

    /**
     * Disable Log Feature
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     */
    public static void disableFeature(final xAuthLogFeatures feature) {
        logFeatures.remove(feature);
    }

    /**
     * Disables all features
     */
    public static void disableFeatures() {
        logFeatures = new ArrayList<xAuthLogFeatures>();
        logFeatures.add(xAuthLogFeatures.NONE);
        restoreFilter();
    }

    /**
     * Set a feature when not already enabled
     * @param feature xAuthLogFeatures NONE, FILTER_COMMANDS
     */
    private static void setFeature(final xAuthLogFeatures feature) {
        if (isFeatureEnabled(feature))
            return;

        if (isFeatureEnabled(xAuthLogFeatures.NONE))
            disableFeature(xAuthLogFeatures.NONE);

        logFeatures.add(feature);
    }

    private static void setFilterCommands() {
        Map<String, Map<String, Object>> commandsMap = xAuth.getPlugin().getDescription().getCommands();

        commandsFilterList.addAll(commandsMap.keySet());
        for (String commandName: commandsMap.keySet()) {
            commandsFilterList.addAll(xAuth.getPlugin().getCommand(commandName).getAliases());
        }
    }

    public static List<xAuthLogFeatures> getFeatures() {
        return logFeatures;
    }

    private static void setFilterClass(final Filter cf) {
        logFilter = cf;
    }

    public static void restoreFilter() {
        removeMincecraftCoreFilter();
    }

    public static void activateCommandFilter() {
        if (!(isFeatureEnabled(xAuthLogFeatures.FILTER_COMMANDS)))
            return;

        // remove the filter first if it exists
        removeMincecraftCoreFilter();

        // add minecraftCoreFilter at runtime
        addMincecraftCoreFilter();
    }

    /**
     * Since message "<player> issued server command: <command> <args>" is no longer part of PlayerCommandPreProcessEvent
     * and was moved to log4j2 before the event is actually called. We need to attach ourselfs to that specific logging instance
     * and add a filter.
     *
     * We do only filter xAuth commands such as login, register and so on that may include sensitive information.
     * See commandsFilterExcludeList for excluded commands.
     */
    private static void addMincecraftCoreFilter() {
        // get the logger that produces the message and get the configuration instance
        org.apache.logging.log4j.core.Logger mcCoreLogger = getMinecraftCoreLogger();
        org.apache.logging.log4j.core.LoggerContext mcCoreLoggerContext = (org.apache.logging.log4j.core.LoggerContext) mcCoreLogger.getContext();
        org.apache.logging.log4j.core.config.BaseConfiguration mcCoreLoggerConfiguration = (org.apache.logging.log4j.core.config.BaseConfiguration) mcCoreLoggerContext.getConfiguration();

        //<RegexFilter regex=".*:\s/(register|login|logout|quit|changepw|xauth|l|q|changepassword|changepass|cpw|x).*" onMatch="DENY" onMismatch="NEUTRAL"/>
        //<RegexFilter regex=regex onMatch="DENY" onMismatch="NEUTRAL"/>

        // create a RegexFilter
        createCommandFilter();

        info("Adding xAuthLog4jCommandFilter to MinecraftCoreLogger ...");

        mcCoreLoggerConfiguration.addLoggerFilter(mcCoreLogger, commandFilter);

        info("Done adding xAuthLog4jCommandFilter to MinecraftCoreLogger.");
    }

    private static void createCommandFilter() {
        // build regex
        info("Building xAuthLog4jCommandFilter RegularExpression ...");
        StringBuilder sb = new StringBuilder();

        // remove all elements from commandsFilterList that are excluded
        commandsFilterList.removeAll(commandsFilterExcludeList);
        for (String command: commandsFilterList) {
            sb.append(command).append("|");
        }
        sb.deleteCharAt(sb.length()-1);

        String regex = ".*:\\s/(" + sb.toString() + ").*";

        info("Done building xAuthLog4jCommandFilter RegularExpression. Creating filter ...");

        // use a RegexFilter
        commandFilter = RegexFilter.createFilter(
                regex,
                "true",
                org.apache.logging.log4j.core.Filter.Result.DENY.name(),
                org.apache.logging.log4j.core.Filter.Result.NEUTRAL.name()
        );

        info("Filter is now ready to use.");
    }

    public static void removeMincecraftCoreFilter() {
        try {
            // get the logger that produces the message and get the configuration instance
            org.apache.logging.log4j.core.Logger mcCoreLogger = getMinecraftCoreLogger();
            org.apache.logging.log4j.core.LoggerContext mcCoreLoggerContext = (org.apache.logging.log4j.core.LoggerContext) mcCoreLogger.getContext();
            org.apache.logging.log4j.core.config.BaseConfiguration mcCoreLoggerConfiguration = (org.apache.logging.log4j.core.config.BaseConfiguration) mcCoreLoggerContext.getConfiguration();

            info("Trying to remove RegexFilter from MinecraftCoreLogger.");

            if (commandFilter == null) {
                createCommandFilter();
            }

            // remove the filter if it is attached
            String name = mcCoreLogger.getName();
            org.apache.logging.log4j.core.config.LoggerConfig lc = mcCoreLoggerConfiguration.getLogger(name);
            if (lc.getName().equals(name)) {
                lc.removeFilter(commandFilter);
            }

            info("Filter removed.");
        } catch (NullPointerException e) {
            warning("RegExFilter not found.");
        }
    }

    private static String getMinecraftLoggerClassName() {
        String[] packagePath = Bukkit.getServer().getClass().getPackage().getName().split("\\.");
        String version = packagePath[packagePath.length - 1];

        return "net.minecraft.server." + version + ".PlayerConnection";
    }

    public static org.apache.logging.log4j.Logger getMinecraftLogger() {
        return org.apache.logging.log4j.LogManager.getLogger(getMinecraftLoggerClassName());
    }

    public static org.apache.logging.log4j.core.Logger getMinecraftCoreLogger() {
        return (org.apache.logging.log4j.core.Logger) getMinecraftLogger();
    }

    public static String getLoggerName() {
        return loggerName;
    }

    public static void reset() {
        setLevel(defaultLevel);
    }

    public static void setLevel(final Level level) {
        logLevel = level;
        logger.setLevel(logLevel);
    }

    public static Level getLevel() {
        return logLevel;
    }

    public static void info(final String msg) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void fine(final String msg) {
        logger.log(Level.FINE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void finer(final String msg) {
        logger.log(Level.FINER, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void finest(final String msg) {
        logger.log(Level.FINEST, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void warning(final String msg) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void info(final String msg, final Throwable e) {
        logger.log(Level.INFO, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }

    public static void warning(final String msg, final Throwable e) {
        logger.log(Level.WARNING, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }

    public static void debug(final String msg) {
        finest(msg);
    }

    public static void severe(final String msg) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg));
    }

    public static void severe(final String msg, final Throwable e) {
        logger.log(Level.SEVERE, "[" + getLoggerName() + "] " + xAuthUtils.replaceColors(msg), e);
    }
}