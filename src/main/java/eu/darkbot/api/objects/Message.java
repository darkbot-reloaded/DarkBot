package eu.darkbot.api.objects;

/**
 * Messages for ChatAPI
 */
public interface Message {
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
