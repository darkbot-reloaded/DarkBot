package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.objects.Gui;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * API for chat
 *
 * @see Message
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
     * @param chatType the {@link Type} of chat
     * @return last 150 messages of given chat {@link Type} otherwise {@code null}
     */
    @Nullable
    Collection<Message> getMessages(Type chatType);

    /**
     * Types or categories of chats
     */
    enum Type {
        GLOBAL, NEWBIE, FACTION, CLAN, GROUP, UBA, RZONE, PRIVATE
    }

    /**
     * Messages for {@link ChatAPI}
     */
    interface Message {
        String getMessage();
        String getUsername();

        /**
         * The type of message.
         * For example, "system" refers to system messages, whereas
         * "chatter" will refer to normal messages sent by players.
         *
         * @return a String specifying the message type
         */
        String getType();

        /**
         * @return the clan tag, empty string if clanless
         */
        String getClanTag();

        String getGlobalId();
        String getUserId();
    }
}
