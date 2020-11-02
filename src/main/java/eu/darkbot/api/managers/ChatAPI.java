package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;
import eu.darkbot.api.objects.Message;
import org.jetbrains.annotations.Nullable;

/**
 * API for chat
 * @see eu.darkbot.api.objects.Message
 */
public interface ChatAPI extends Gui, API {
    /**
     * Gets the last message sent from the specified chat.
     *
     * @param chatType  the {@code Type} of chat
     * @return the most recent message from the {@code chatType},
     *         null if the {@code chatType} doesn't exist
     * @see Type
     */
    @Nullable
    Message getLastMessage(Type chatType);

    /**
     * Types or categories of chats
     */
    enum Type { //TODO: PROBABLY MORE CHATS THAT I FORGOT
        GLOBAL, COMPANY, CLAN, GROUP
    }
}
