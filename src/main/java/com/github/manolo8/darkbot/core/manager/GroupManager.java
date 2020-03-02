package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.swf.Dictionary;
import com.github.manolo8.darkbot.core.objects.swf.group.Group;
import com.github.manolo8.darkbot.core.objects.swf.group.GroupMember;
import com.github.manolo8.darkbot.core.objects.swf.group.Invite;

import java.util.ArrayList;
import java.util.List;

import static com.github.manolo8.darkbot.Main.API;

public class GroupManager extends Gui {
    private static final int HEADER_HEIGHT = 26; // Height of the top margin of the group
    private static final int BUTTON_HEIGHT = 28; // Height of one invite or button set
    private static final int MEMBER_HEIGHT = 48; // Height of one member

    private static final int MARGIN_WIDTH = 10; // Margin from left to first button
    private static final int BUTTON_WIDTH = 30; // Width of each button
    private static final int INVITE_WIDTH = 130;// Width used by invites, accept or cancel/decline buttons after it

    private Main main;
    private Config configRoot;
    private Config.GroupSettings config;
    private MapManager mapManager;

    public Group group;
    public boolean pinging; // If the pinging button is enabled & you're ready to ping
    public List<Invite> invites = new ArrayList<>();

    private Dictionary inviteDict = new Dictionary();

    private long nextAction;

    private Runnable pending;

    public GroupManager(Main main) {
        this.main = main;
        this.configRoot = main.config;
        this.config = main.config.GROUP;
        this.mapManager = main.mapManager;

        this.group = new Group(main.hero);
    }

    @Override
    public void update() {
        super.update();

        if (address == 0 || mapManager.eventAddress == 0) return;

        long groupAddress = API.readMemoryLong(API.readMemoryLong(mapManager.eventAddress) + 0x48);
        if (groupAddress == 0) return;
        group.update(API.readMemoryLong(groupAddress + 0x30));

        pinging = API.readMemoryBoolean(groupAddress + 0x40);
        inviteDict.update(API.readMemoryLong(groupAddress + 0x48));
        inviteDict.update();

        synchronized (Main.UPDATE_LOCKER) {
            if (invites.size() > inviteDict.size) invites = invites.subList(0, inviteDict.size);
            for (int i = 0; i < inviteDict.size; i++) {
                while (invites.size() <= i) invites.add(new Invite(main.hero));
                invites.get(i).update(inviteDict.get(i).value);
            }
        }
    }

    public void tick() {
        if (group.address == 0) return;
        if (nextAction > System.currentTimeMillis()) return;
        nextAction = System.currentTimeMillis() + 100;

        tryQueueAcceptInvite();
        tryOpenInvites();
        tryQueueSendInvite();

        if (pending == null) show(false);
        else {
            if (!show(true)) return;
            pending.run();
            nextAction = System.currentTimeMillis() + 500;
            pending = null;
        }
    }

    public void tryQueueAcceptInvite() {
        if (pending != null || !config.ACCEPT_INVITES || invites.isEmpty() || group.isValid()) return;

        invites.stream()
                .filter(in -> in.incomming && (config.WHITELIST_TAG == null ||
                        config.WHITELIST_TAG.has(main.config.PLAYER_INFOS.get(in.inviter.id))))
                .findFirst()
                .ifPresent(inv -> pending = () ->
                        clickBtn(MARGIN_WIDTH + INVITE_WIDTH, 0, HEADER_HEIGHT + BUTTON_HEIGHT, invites.indexOf(inv)));
    }

    public void tryOpenInvites() {
        if (pending != null || !group.isValid() || !group.isLeader) return;

        if (group.isOpen != config.OPEN_INVITES) pending = () -> click(GroupAction.CAN_INVITE);
    }

    public void tryQueueSendInvite() {
        if (pending != null || !canInvite() || config.INVITE_TAG == null) return;

        for (PlayerInfo value : configRoot.PLAYER_INFOS.values()) {
            if (!config.INVITE_TAG.has(value)) continue;

            /*pending = () -> {
                click(MARGIN_WIDTH + (INVITE_WIDTH / 2), HEADER_HEIGHT + getGroupHeight() - (BUTTON_HEIGHT / 2));
                API.sendText(value.username);
                click(MARGIN_WIDTH + INVITE_WIDTH + (BUTTON_WIDTH / 2), HEADER_HEIGHT + getGroupHeight() - (BUTTON_HEIGHT / 2));
            };*/
            break;
        }
    }

    public void kick(int id) {
        kick(group.getMember(id));
    }

    public void kick(GroupMember member) {
        if (member != null && canKick()) kickUser(group.indexOf(member));
    }

    private void kickUser(int idx) {
        if (idx <= 0) return;

        click(GroupAction.REMOVE);
        // TODO: wait between clicks
        click((int) size.x / 2, (MEMBER_HEIGHT / 2) + idx * MEMBER_HEIGHT);
    }

    public boolean canKick() {
        return group.isValid() && group.isLeader;
    }

    public boolean canInvite() {
        return (!group.isValid() || group.isOpen || group.isLeader) && invites.size() + group.size < 8;
    }

    private void click(GroupAction action) {
        clickBtn(MARGIN_WIDTH, action.idx(group.isLeader), HEADER_HEIGHT + getGroupHeight(), 0);
    }

    private int getGroupHeight() {
        return Math.max(0, group.size - 1) * MEMBER_HEIGHT +
                (invites.size() * BUTTON_HEIGHT) +
                (canInvite() ? BUTTON_HEIGHT : 0);
    }

    private int offset(int margin, int offset, int index) {
        return margin + (offset * index) + (offset / 2);
    }

    private void clickBtn(int marginX, int indexX,
                          int marginY, int indexY) {
        click(offset(marginX, BUTTON_WIDTH, indexX), offset(marginY, BUTTON_HEIGHT, indexY));
    }

    private enum GroupAction {
        LEAVE(0),
        CAN_INVITE(-1),
        PING(1),
        FOLLOW(2),
        CROWN(-1),
        REMOVE(-1);

        private int notLeaderPos;

        GroupAction(int notLeaderPos) {
            this.notLeaderPos = notLeaderPos;
        }

        public int idx(boolean leader) {
            return leader ? ordinal() : notLeaderPos;
        }
    }

}