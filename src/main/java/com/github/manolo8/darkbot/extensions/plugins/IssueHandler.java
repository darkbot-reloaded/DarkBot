package com.github.manolo8.darkbot.extensions.plugins;

import com.github.manolo8.darkbot.core.utils.Lazy;
import eu.darkbot.api.extensions.FeatureException;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IssueHandler {
    private final IssueHandler parent;

    private final Set<PluginIssue> issues = new TreeSet<>();
    private final Lazy<IssueHandler> listener = new Lazy.NoCache<>();
    private final Lazy<IssueHandler> uiListener = new Lazy.Swing<>();

    public IssueHandler() {
        this(null);
    }

    public IssueHandler(IssueHandler parent) {
        this.parent = parent;
        this.listener.add(uiListener::send);
    }

    public void handleTickFeatureException(PluginIssue.Level defaultLevel, Throwable e) {
        handleFeatureException(defaultLevel, "bot.issue.feature.failed_to_tick", e);
    }

    /**
     * @return true if feature have any critical issue, false otherwise
     */
    public void handleFeatureException(PluginIssue.Level defaultLevel, String defaultMessage, Throwable e) {
        FeatureException fe = FeatureException.find(e);
        if (fe != null) {
            String message = fe.getMessage();
            String description = fe.getDescription() == null ? createDescription(fe.getCause()) : fe.getDescription();
            PluginIssue.Level level = PluginIssue.Level.values()[fe.getLevel().ordinal()];

            add(new PluginIssue(message, description, level, fe));
        } else {
            add(new PluginIssue(defaultMessage, IssueHandler.createDescription(e), defaultLevel, e));
        }
    }

    public void addInfo(String message, String description) {
        add(message, description, PluginIssue.Level.INFO);
    }

    public void addWarning(String message, String description) {
        add(message, description, PluginIssue.Level.WARNING);
    }

    public void addFailure(String message, String description) {
        add(message, description, PluginIssue.Level.ERROR);
    }

    public static String createDescription(Throwable e) {
        return createDescription(e, false)
                .collect(Collectors.joining("<br>", "<html>", "</html>"));
    }

    private static Stream<String> createDescription(Throwable e, boolean isCause) {
        Stream<String> stream = Stream.concat(
                Stream.of("<strong>" + (isCause ? "Caused By: " : "") + e.toString() + "</strong>"),
                Arrays.stream(e.getStackTrace()).map(IssueHandler::toSimpleString)
        ).limit(100);
        if (e.getCause() == null) return stream;
        return Stream.concat(stream, createDescription(e.getCause(), true));
    }

    /**
     * Converts a StackTraceElement to a simple string.
     * If filenames are long and have no line number they're assumed to be obfuscated and are removed.
     */
    private static String toSimpleString(StackTraceElement ste) {
        String code;
        if (ste.isNativeMethod()) code = "Native Method";
        else if (ste.getFileName() != null && ste.getLineNumber() >= 0)
            code = ste.getFileName() + ":" + ste.getLineNumber();
        else if (ste.getFileName() != null && ste.getFileName().length() < 100)
            code = ste.getFileName();
        else code = "Unknown Source";

        return ste.getClassName() + "." + ste.getMethodName() + "(" + code + ")";
    }

    public void add(PluginIssue pluginIssue) {
        if (issues.add(pluginIssue)) {
            listener.send(this);
            pluginIssue.increaseAndPrint();
        } else {
            for (PluginIssue issue : issues) {
                if (issue.equals(pluginIssue)) {
                    issue.increaseAndPrint();
                    break;
                }
            }
        }
    }

    public void add(String messageKey, String description, PluginIssue.Level level) {
        add(new PluginIssue(messageKey, description, level));
    }

    public void addListener(Consumer<IssueHandler> listener) {
        this.listener.add(listener);
    }

    public void addUIListener(Consumer<IssueHandler> listener) {
        this.uiListener.add(listener);
    }

    public Set<PluginIssue> getIssues() {
        return issues;
    }

    /**
     * @return The highest issue level.
     */
    public PluginIssue.Level getLevel() {
        return issues.isEmpty() ? null : issues.iterator().next().getLevel();
    }

    public boolean canLoad() {
        return getLevel() != PluginIssue.Level.ERROR && (parent == null || parent.canLoad());
    }

}
