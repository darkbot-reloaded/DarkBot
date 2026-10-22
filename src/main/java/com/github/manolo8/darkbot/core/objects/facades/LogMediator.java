package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.Lazy;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.EventBrokerAPI;
import eu.darkbot.api.managers.GameLogAPI;

import static com.github.manolo8.darkbot.Main.API;

public class LogMediator extends Updatable implements GameLogAPI, Listener {
    /** Slot of the _messageBuffer array in LogWindowMediator (0x60 holds _logWindow in current clients). */
    private static final int MESSAGE_BUFFER = 0x70;
    /** Log message objects are small, there is nothing useful past this offset. */
    private static final int MAX_TEXT_OFFSET = 0x68;

    @Deprecated
    public final Lazy<String> logs = new Lazy.NoCache<>(); // Can't cache the value, same log could appear twice

    private final EventBrokerAPI eventBroker;
    private final FlashListLong messageBuffer = FlashListLong.ofArray();

    /** Offset inside a log message object that points to its text, resolved on first use. */
    private int textOffset = -1;

    public LogMediator(EventBrokerAPI eventBroker) {
        this.eventBroker = eventBroker;
        this.eventBroker.registerListener(this);
    }

    @Override
    public void update() {
        messageBuffer.update(API.readLong(address + MESSAGE_BUFFER));
        if (messageBuffer.isEmpty() || messageBuffer.size() > 50) return;

        messageBuffer.forEachIncremental(this::handleLogMessage);
    }

    private void handleLogMessage(long pointer) {
        String val = readLogText(pointer);
        if (val != null && !val.trim().isEmpty())
            eventBroker.sendEvent(new LogMessageEvent(val));
    }

    /**
     * Reads the text of a log message. The game client moves the fields of the message class
     * around between updates (the text used to be at 0x28), so the slot is resolved at runtime
     * instead of being hardcoded.
     */
    private String readLogText(long pointer) {
        if (textOffset >= 0) {
            String text = readText(pointer, textOffset);
            if (text != null) return text;
        }

        String best = null;
        int bestOffset = -1;
        for (int offset = 0; offset <= MAX_TEXT_OFFSET; offset += 8) {
            String text = readText(pointer, offset);
            if (text == null || (best != null && text.length() <= best.length())) continue;
            best = text;
            bestOffset = offset;
        }

        if (best == null) return null;
        return setTextOffset(bestOffset, best);
    }

    private String setTextOffset(int offset, String text) {
        if (textOffset != offset) {
            textOffset = offset;
            System.out.println("Log message text found at offset 0x" + Integer.toHexString(offset));
        }
        return text;
    }

    private String readText(long pointer, int offset) {
        long string = API.readLong(pointer + offset);
        if (string == 0 || (string & 7) != 0) return null;

        String text = API.readStringDirect(string, null);
        return text != null && isPlausibleText(text) ? text : null;
    }

    private static boolean isPlausibleText(String text) {
        if (text.length() < 4 || text.length() > 512) return false;
        // Log messages are user facing text, not links or internal identifiers
        if (text.startsWith("http")) return false;

        boolean hasLetter = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (Character.isISOControl(c)) return false;
            if (Character.isLetter(c)) hasLetter = true;
        }
        return hasLetter;
    }

    @EventHandler
    public void onLogMessage(LogMessageEvent e) {
        System.out.println(e.getMessage());
        logs.send(e.getMessage());
    }

}
