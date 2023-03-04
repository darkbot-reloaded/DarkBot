package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Group extends Updatable.Auto {
    private final HeroManager hero;

    public List<GroupMember> members = new ArrayList<>();
    public GroupMember selectedMember = new GroupMember();
    public GroupMember heroMember = new GroupMember();

    public int id;
    public int maxSize;
    public boolean isOpen; // if the group is open to allowing anyone to invite
    public boolean isLeader;

    private ObjArray membersPtr = ObjArray.ofVector(true);

    public Group(HeroManager hero) {
        this.hero = hero;
    }

    public boolean isValid() {
        return id != 0 && maxSize == 8;
    }

    @Override
    public void update() {
        id = API.readMemoryInt(address + 0x1F);
//        size = API.readMemoryInt(address + 0x23);
        maxSize = API.readMemoryInt(address + 0x27);
        isOpen = API.readMemoryBoolean(address + 0x2B);

        if (!isValid()) {
            if (!members.isEmpty()) reset();
            return;
        }

        long selectedAddr = API.readMemoryLong(address + 0x3F);

        membersPtr.update(API.readMemoryLong(address + 0x37));

        List<GroupMember> filtered;
        synchronized (Main.UPDATE_LOCKER) {
            filtered = membersPtr.sync(members, GroupMember::new, m -> m.id != hero.id);
        }
        heroMember = filtered.stream().findFirst().orElse(null);
        isLeader = heroMember != null && heroMember.isLeader;
        selectedMember = members.stream().filter(m -> selectedAddr == m.address).findFirst().orElse(null);
    }

    private void reset() {
        members.clear();
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
