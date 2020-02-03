package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.itf.Manager;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;
import com.github.manolo8.darkbot.core.objects.swf.group.Group;
import com.github.manolo8.darkbot.core.objects.swf.group.GroupMember;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.github.manolo8.darkbot.Main.API;

public class GroupManager extends Updatable implements Manager {
    private long eventAddress = -1;

    public Group group = new Group();
    public List<Invite> invites = new ArrayList<>();

    private Dictionary dictionary = new Dictionary(0);

    @Override
    public void install(BotInstaller botInstaller) {
        botInstaller.screenManagerAddress.add(value -> this.eventAddress = value + 200);
    }

    public void tick() {
        if (eventAddress == -1) return;

        update(API.readMemoryLong(API.readMemoryLong(eventAddress) + 0x48));
        update();
    }

    @Override
    public void update() {
        if (this.address == 0) return;

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