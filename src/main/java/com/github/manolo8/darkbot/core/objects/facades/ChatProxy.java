package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.LogUtils;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.managers.ChatAPI;
import eu.darkbot.api.managers.EventBrokerAPI;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class ChatProxy extends Updatable implements ChatAPI, Listener {

    private final ObjArray chatsArr = ObjArray.ofVector(true);
    private final List<ChatRoom> chats = new ArrayList<>();

    private final Map<String, OutputStream> fileWriters = new HashMap<>();

    private final EventBrokerAPI eventBroker;

    public ChatProxy(EventBrokerAPI eventBroker) {
        this.eventBroker = eventBroker;
        this.eventBroker.registerListener(this);
    }

    @Override
    public void update() {
        long data = API.readMemoryLong(address + 48) & ByteUtils.ATOM_MASK;

        this.chatsArr.update(API.readMemoryLong(data + 64));
        if (chatsArr.getSize() > 20) return;
        this.chatsArr.sync(chats, ChatRoom::new);

        for (ChatRoom chat : chats) {
            if (chat.messagesArr.getSize() > 150) continue;
            chat.messagesArr.forEachIncremental(ptr ->
                    eventBroker.sendEvent(new MessageSentEvent(chat.chatName, new Message(ptr))));
        }
    }

    public static class ChatRoom extends Auto {
        private String chatName;
        private final ObjArray messagesArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.chatName = API.readMemoryString(address, 56);
            this.messagesArr.update(API.readMemoryLong(address + 80));
        }
    }

    public static class Message extends Auto implements ChatAPI.Message {
        public String message, username, role, clanTag, globalId, userId;

        public Message(long address) {
            update(address);
        }

        @Override
        public void update() {
            this.message  = API.readMemoryString(address, 40);
            this.username = API.readMemoryString(address, 56, 32);
            this.role     = API.readMemoryString(address, 56, 40);
            this.clanTag  = API.readMemoryString(address, 56, 48);
            this.globalId = API.readMemoryString(address, 56, 56);
            this.userId   = API.readMemoryString(address, 56, 64);
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