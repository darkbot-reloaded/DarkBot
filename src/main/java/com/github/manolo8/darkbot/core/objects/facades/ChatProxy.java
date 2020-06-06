package com.github.manolo8.darkbot.core.objects.facades;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.LogUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class ChatProxy extends Updatable {
    public List<Chat> chats = new ArrayList<>();

    private ObjArray chatsArr = ObjArray.ofVector(true);
    private Map<String, OutputStream> streams = new HashMap<>();

    private int clanLength = 7, nameLength = 10;

    @Override
    public void update() {
        if (!ConfigEntity.INSTANCE.getConfig().MISCELLANEOUS.LOG_CHAT) return;
        long data = API.readMemoryLong(address + 48) & ByteUtils.FIX;

        this.chatsArr.update(API.readMemoryLong(data + 64));
        if (chatsArr.getSize() > 10) return;
        this.chatsArr.sync(chats, Chat::new, null);

        for (Chat chat : chats) {
            if (chat.messagesArr.getSize() > 150) continue;
            chat.messagesArr.forEachIncremental(ptr -> writeToFile(chat.chatName, new Message(ptr)));
        }
    }

    private void writeToFile(String chatName, Message message) {
        try {
            OutputStream os = getOrCreateStream(chatName);
            if (os == null) return;

            os.write(formatMessage(message).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatMessage(Message message) {
        if (message.clanTag.length() + 2 > clanLength) clanLength = message.clanTag.length() + 2;
        if (message.username.length() > nameLength) nameLength = message.username.length();

        return String.format("[%s] |%-7s, %-9s| %-" + clanLength + "s%-" + nameLength + "s: %s" + System.lineSeparator(),
                LocalDateTime.now().format(LogUtils.LOG_DATE),
                message.role,
                message.userId,
                "[" + message.clanTag + "]",
                message.username,
                message.message);
    }

    private OutputStream getOrCreateStream(String chatName) {
        return this.streams.computeIfAbsent(chatName, LogUtils::createLogFile);
    }

    public static class Chat extends UpdatableAuto {
        public String chatName;
        //public List<Message> messages = new ArrayList<>();

        private ObjArray messagesArr = ObjArray.ofVector(true);

        @Override
        public void update() {
            this.chatName = API.readMemoryString(address, 56);

            this.messagesArr.update(API.readMemoryLong(address + 80));
            //this.messagesArr.sync(messages, Message::new, null);
        }
    }

    public static class Message extends UpdatableAuto {
        public String message, username, role, clanTag, globalId, userId;

        public Message() {}
        private Message(long address) { update(address); }

        @Override
        public void update() {
            this.message  = API.readMemoryString(address, 40);
            this.username = API.readMemoryString(address, 56, 32);
            this.role     = API.readMemoryString(address, 56, 40);
            this.clanTag  = API.readMemoryString(address, 56, 48);
            this.globalId = API.readMemoryString(address, 56, 56);
            this.userId   = API.readMemoryString(address, 56, 64);
        }
    }
}