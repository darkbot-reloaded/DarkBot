package com.github.manolo8.darkbot.core.objects.swf.group;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.manolo8.darkbot.Main.API;

public class GroupManager extends Updatable {
    public Group group = new Group();
    public List<Invite> invites = new ArrayList<>();

    private Dictionary dictionary = new Dictionary(0);

    @Override
    public void update() {// API.readMemoryLong(API.readMemoryLong(MapManager.eventAddress) + 0x48) == this.address
        group.update(API.readMemoryLong(address + 0x30));
        group.update();

        long dictionaryAddress = API.readMemoryLong(address + 0x48);
        if (dictionary.address != dictionaryAddress) dictionary.update(dictionaryAddress);
        dictionary.update();

        invites.clear();
        Arrays.stream(dictionary.elements).filter(Objects::nonNull).forEach(entry -> {
            Invite invite = new Invite();
            invite.update(entry.value);
            invite.update();
            invites.add(invite);
        });
    }


    public static class Invite extends Updatable {
        GroupMember inviter = new GroupMember();
        GroupMember invited = new GroupMember();

        @Override
        public void update() {
            inviter.update(API.readMemoryLong(address + 0x20)); //only id and username is updated
            invited.update(API.readMemoryLong(address + 0x28)); //only id and username is updated
            inviter.update();
            invited.update();

            //probably better is just to change inviterId = int, inviterUsername = str etc.
        }
    }
}