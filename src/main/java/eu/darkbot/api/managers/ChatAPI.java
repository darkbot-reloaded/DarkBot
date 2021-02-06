package eu.darkbot.api.managers;

import eu.darkbot.api.API;
import eu.darkbot.api.events.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * API for chat
 *
 * @see Message
 */
public interface ChatAPI extends API.Singleton {

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
        /**
         * @return The id of the user who sent the message
         */
        String getUserId();

        /**
         * @return The user who sent the message
         */
        String getUsername();

        /**
         * @return the clan tag, empty string if clanless
         */
        String getClanTag();

        /**
         * The type of message.
         * For example, "system" refers to system messages, whereas
         * "chatter" will refer to normal messages sent by players.
         *
         * @return a String specifying the message type
         */
        String getType();

        /**
         * @return The content of the message itself
         */
        String getMessage();

        String getGlobalId();


        DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("uuuu/MM/dd HH:mm:ss.SSS");
        AtomicInteger MAX_CLAN_LEN = new AtomicInteger(7);
        AtomicInteger MAX_NAME_LEN = new AtomicInteger(10);

        default String formatted() {
            int clan = MAX_CLAN_LEN.updateAndGet(curr -> Math.max(curr, getClanTag().length() + 2));
            int name = MAX_NAME_LEN.updateAndGet(curr -> Math.max(curr, getUsername().length()));

            return String.format("[%s] |%-7s, %-9s| %-" + clan + "s%-" + name + "s: %s" + System.lineSeparator(),
                    LocalDateTime.now().format(DATE_FORMAT),
                    getType(),
                    getUserId(),
                    "[" + getClanTag() + "]",
                    getUsername(),
                    getMessage());
        }
    }

    class MessageSentEvent implements Event {
        private final String room;
        private final Message message;

        public MessageSentEvent(String room, Message message) {
            this.room = room;
            this.message = message;
        }

        public String getRoom() {
            return room;
        }

        public Message getMessage() {
            return message;
        }
    }
}
