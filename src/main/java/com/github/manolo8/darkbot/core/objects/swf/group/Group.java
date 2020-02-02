package com.github.manolo8.darkbot.core.objects.swf.group;

import com.github.manolo8.darkbot.core.itf.Updatable;
import com.github.manolo8.darkbot.core.objects.swf.Array;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class Group extends Updatable {
    public List<GroupMember> members = new ArrayList<>();
    public GroupMember selectedMember = new GroupMember();

    public int id;
    public int size;
    public int maxSize;
    public boolean canInvite; // is invite button triggered or no

    private Array array = new Array(0);

    @Override
    public void update() {
        id        = API.readMemoryInt(address + 0x1F);
        size      = API.readMemoryInt(address + 0x23);
        maxSize   = API.readMemoryInt(address + 0x27);
        canInvite = API.readMemoryBoolean(address + 0x2B);

        selectedMember.update(API.readMemoryLong(address + 0x3F));
        selectedMember.update();

        array.update(API.readMemoryLong(address + 0x37));
        array.update();

        members.clear();
        for (int i = 0; i < array.elements.length; i++) {
            GroupMember groupMember = new GroupMember();
            groupMember.update(array.elements[i]);
            groupMember.update();
            members.add(groupMember);
        }
    }
}
