package com.github.manolo8.darkbot.core.objects.group;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.swf.ObjArray;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Group extends UpdatableAuto {
    private final HeroManager hero;

    public List<GroupMember> members = new ArrayList<>();
    public GroupMember selectedMember = new GroupMember();

    public int id;
    public int size;
    public int maxSize;
    public boolean isOpen; // if the group is open to allowing anyone to invite
    public boolean isLeader;

    private ObjArray membersPtr = ObjArray.ofVector(true);

    public Group(HeroManager hero) {
        this.hero = hero;
    }

    public boolean isValid() {
        return id != 0 && size != 0 && maxSize == 8;
    }

    @Override
    public void update() {
        id      = API.readMemoryInt(address + 0x1F);
        size    = API.readMemoryInt(address + 0x23);
        maxSize = API.readMemoryInt(address + 0x27);
        isOpen  = API.readMemoryBoolean(address + 0x2B);

        if (!isValid()) return;

        long selectedAddr = API.readMemoryLong(address + 0x3F);

        membersPtr.update(API.readMemoryLong(address + 0x37));

        List<GroupMember> filtered;
        synchronized (Main.UPDATE_LOCKER) {
            filtered = membersPtr.sync(members, GroupMember::new, m -> m.id != hero.id);
        }
        isLeader = filtered.stream().map(h -> h.isLeader).findFirst().orElse(false);
        selectedMember = members.stream().filter(m -> selectedAddr == m.address).findFirst().orElse(null);
    }

    public GroupMember getMember(int id) {
        for (GroupMember member : members) {
            if (member.id == id) return member;
        }
        return null;
    }

    public int indexOf(GroupMember member) {
        for (int i = 0; i < size; i++)
            if (members.get(i) == member) return i;
        return -1;
    }

}
