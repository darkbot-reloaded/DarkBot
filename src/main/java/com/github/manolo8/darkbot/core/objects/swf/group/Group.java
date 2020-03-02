package com.github.manolo8.darkbot.core.objects.swf.group;

import com.github.manolo8.darkbot.core.itf.UpdatableAuto;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.objects.swf.VectorPtr;

import static com.github.manolo8.darkbot.Main.API;

public class Group extends UpdatableAuto {
    private final HeroManager hero;

    public GroupMember[] members = new GroupMember[0];
    private GroupMember overflowMember = new GroupMember();
    public GroupMember selectedMember = new GroupMember();

    public int id;
    public int size;
    public int maxSize;
    public boolean isOpen; // if the group is open to allowing anyone to invite
    public boolean isLeader;

    private VectorPtr membersPtr = new VectorPtr(0);

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
        membersPtr.update();

        // The amount of other people on the group, always minus 1 (yourself)
        int grpSize = Math.max(0, membersPtr.size - 1);

        if (members.length != grpSize) {
            members = new GroupMember[grpSize];
            for (int i = 0; i < members.length; i++) members[i] = new GroupMember();
        }
        selectedMember = null;
        for (int i = 0, arrIdx = 0; arrIdx < membersPtr.size; i++, arrIdx++) {
            GroupMember member = i < members.length ? members[i] : overflowMember;
            member.update(membersPtr.elements[arrIdx]);
            if (member.id == hero.id) {
                this.isLeader = member.isLeader;
                i--;
            }
            if (selectedAddr == member.address) selectedMember = member;
        }
    }

    public GroupMember getMember(int id) {
        for (GroupMember member : members) {
            if (member.id == id) return member;
        }
        return null;
    }

    public int indexOf(GroupMember member) {
        for (int i = 0; i < size; i++)
            if (members[i] == member) return i;
        return -1;
    }

}
