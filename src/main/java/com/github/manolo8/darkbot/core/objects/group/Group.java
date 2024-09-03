package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.swf.FlashList;

import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Group extends Updatable.Auto {
    public final List<GroupMember> members;
    public GroupMember selectedMember = new GroupMember();
    public GroupMember heroMember = new GroupMember();

    public int id;
    public int maxSize;
    public boolean isOpen; // if the group is open to allowing anyone to invite
    public boolean isLeader;

    private final HeroManager hero;
    private final FlashList<GroupMember> allMembers = FlashList.ofVector(GroupMember::new);

    public Group(HeroManager hero) {
        this.hero = hero;
        this.members = allMembers.asFiltered(m -> m.id != hero.id);
    }

    public boolean isValid() {
        return id != 0 && maxSize == 8;
    }

    @Override
    public void update() {
        id = API.readInt(address + 0x1F);
//        size = API.readMemoryInt(address + 0x23);
        maxSize = API.readInt(address + 0x27);
        isOpen = API.readBoolean(address + 0x2B);

        if (!isValid()) {
            if (!allMembers.isEmpty()) reset();
            return;
        }

        long selectedAddr = API.readLong(address + 0x3F);
        synchronized (Main.UPDATE_LOCKER) {
            allMembers.update(API.readLong(address + 0x37));
        }

        heroMember = allMembers.stream().filter(m -> m.id == hero.id).findAny().orElse(null);
        selectedMember = allMembers.stream().filter(m -> selectedAddr == m.address).findFirst().orElse(null);

        isLeader = heroMember != null && heroMember.isLeader;
    }

    private void reset() {
        synchronized (Main.UPDATE_LOCKER) {
            allMembers.update(0);
        }
        isLeader = false;
        heroMember = null;
        selectedMember = null;
    }

    public GroupMember getMember(int id) {
        for (GroupMember member : members) {
            if (member.id == id) return member;
        }
        return null;
    }

    public int indexOf(int id) {
        if (members.isEmpty()) return -1;
        for (int i = 0; i < members.size(); i++)
            if (members.get(i).id == id) return i;
        return -1;
    }

    public int indexOf(GroupMember member) {
        if (members.isEmpty()) return -1;
        for (int i = 0; i < members.size(); i++)
            if (members.get(i) == member) return i;
        return -1;
    }
}
