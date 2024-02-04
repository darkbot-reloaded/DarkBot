package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;
import com.github.manolo8.darkbot.core.objects.swf.FlashListLong;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.LogUtils;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.ChatAPI;
import eu.darkbot.api.managers.EventBrokerAPI;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class ChatProxy extends Updatable implements ChatAPI, Listener {

    private final FlashList<ChatRoom> chats = FlashList.ofVector(ChatRoom::new);

    private final Map<String, OutputStream> fileWriters = new HashMap<>();

    private final EventBrokerAPI eventBroker;

    public ChatProxy(EventBrokerAPI eventBroker) {
        this.eventBroker = eventBroker;
        this.eventBroker.registerListener(this);
    }

    @Override
    public void update() {
        long data = API.readLong(address + 48) & ByteUtils.ATOM_MASK;
        this.chats.update(API.readLong(data + 64));

        for (ChatRoom chat : chats) {
            if (chat.messagesArr.size() > 150) continue;
            chat.messagesArr.forEachIncremental(ptr ->
                    eventBroker.sendEvent(new MessageSentEvent(chat.chatName, new Message(ptr))));
        }
    }

    public static class ChatRoom extends Auto {
        private String chatName;
        private final FlashListLong messagesArr = FlashListLong.ofVector();

        @Override
        public void update() {
            this.chatName = API.readString(address, 56);
            this.messagesArr.update(API.readLong(address + 80));
        }
    }

    public static class Message extends Auto implements ChatAPI.Message {
        public String message, username, role, clanTag, globalId, userId;

        public Message(long address) {
            update(address);
        }

        @Override
        public void update() {
            this.message = readString(40);

            long userAddress = readLong(56);
            this.username = API.readString(userAddress, 32);
            this.role     = API.readString(userAddress, 40);
            this.clanTag  = API.readString(userAddress, "", 48);
            this.globalId = API.readString(userAddress, 56);
            this.userId   = API.readString(userAddress, 64);
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public String getType() {
            return role;
        }

        @Override
        public String getClanTag() {
            return clanTag;
        }

        @Override
        public String getGlobalId() {
            return globalId;
        }

        @Override
        public String getUserId() {
            return userId;
        }
    }

    @EventHandler
    public void onChatMessage(MessageSentEvent event) {
        if (!ConfigEntity.INSTANCE.getConfig().MISCELLANEOUS.LOG_CHAT) return;

        try {
            OutputStream os = fileWriters.computeIfAbsent(event.getRoom(), LogUtils::createLogFile);
            if (os != null) os.write(event.getMessage().formatted().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}