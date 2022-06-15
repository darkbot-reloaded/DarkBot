package com.github.manolo8.darkbot.core.manager;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.core.api.GameAPI;
import com.github.manolo8.darkbot.core.objects.Gui;
import com.github.manolo8.darkbot.core.objects.group.Group;
import com.github.manolo8.darkbot.core.objects.group.GroupMember;
import com.github.manolo8.darkbot.core.objects.group.Invite;
import com.github.manolo8.darkbot.core.objects.swf.PairArray;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.utils.NativeAction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.manolo8.darkbot.Main.API;

public class GroupManager extends Gui implements GroupAPI {
    private static final int HEADER_HEIGHT = 26; // Height of the top margin of the group
    private static final int BUTTON_HEIGHT = 28; // Height of one invite or button set
    private static final int MEMBER_HEIGHT = 48; // Height of one member

    private static final int MARGIN_WIDTH = 10; // Margin from left to first button
    private static final int BUTTON_WIDTH = 30; // Width of each button
    private static final int INVITE_WIDTH = 130;// Width used by invites, accept or cancel/decline buttons after it

    private final Main main;
    private Config.GroupSettings config;

    public Group group;
    public boolean pinging; // If the pinging button is enabled & you're ready to ping
    public List<Invite> invites = new ArrayList<>();

    private final PairArray inviteDict = PairArray.ofDictionary().setAutoUpdatable(true).setIgnoreEmpty(false);

    private long nextAction;
    private Runnable pending;

    private final Map<String, Long> pastInvites = new HashMap<>();
    private int shouldLeave = 0;

    private long lastValidTime; // Last time group had members for isLoaded check

    public GroupManager(Main main) {
        this.main = main;

        this.group = new Group(main.hero);
    }

    @Override
    public void update() {
        super.update();

        if (address == 0) return;

        long groupAddress = main.facadeManager.getProxyAddressOf("GroupProxy");
        if (groupAddress == 0) return;
        group.update(API.readMemoryLong(groupAddress + 0x30));

        pinging = API.readMemoryBoolean(groupAddress + 0x40);
        inviteDict.update(API.readMemoryLong(groupAddress + 0x48));

        inviteDict.sync(invites, () -> new Invite(main.hero), invite -> invite.valid);
    }

    public void tick() {
        if (group.address == 0) return;

        if (group.isValid()) lastValidTime = System.currentTimeMillis();
        else if (System.currentTimeMillis() - lastValidTime < 10_000L) return; // Wait until reacting to group being invalid
        if (nextAction > System.currentTimeMillis()) return;
        nextAction = System.currentTimeMillis() + 100;

        this.config = main.config.GROUP;

        tryQueueLeave();
        tryQueueAcceptInvite();
        tryOpenInvites();
        tryQueueSendInvite();

        if (pending == null) show(false);
        else {
            if (!show(true)) return;
            pending.run();
            nextAction = System.currentTimeMillis() + 1000;
            pending = null;
        }
    }

    public void tryQueueAcceptInvite() {
        if (pending != null || !config.ACCEPT_INVITES || invites.isEmpty() || group.isValid()) return;

        invites.stream()
                .filter(in -> in.valid && in.incomming && (config.WHITELIST_TAG == null ||
                        config.WHITELIST_TAG.has(main.config.PLAYER_INFOS.get(in.inviter.id))))
                .findFirst()
                .ifPresent(inv -> pending = () -> acceptInvite(inv));
    }

    public void tryOpenInvites() {
        if (pending != null || !group.isValid() || !group.isLeader) return;

        if (group.isOpen != config.OPEN_INVITES) pending = () -> click(GroupAction.CAN_INVITE);
    }

    public void tryQueueSendInvite() {
        if (pending != null || !canInvite() || config.INVITE_TAG == null) return;

        for (PlayerInfo player : main.config.PLAYER_INFOS.values()) {
            if (!config.INVITE_TAG.has(player) || group.getMember(player.userId) != null) continue;

            Long inviteTime = pastInvites.get(player.username);
            if (inviteTime != null && System.currentTimeMillis() < inviteTime) continue;

            pending = () -> sendInvite(player.username, inviteTime == null ? 60_000 : 120_000);
            break;
        }
    }

    public void tryQueueLeave() {
        if (pending != null) return;

        if (shouldLeave()) shouldLeave = Math.min(20, shouldLeave + 1);
        else shouldLeave = 0;

        if (shouldLeave >= 20)
            pending = () -> click(GroupAction.LEAVE);
    }

    public boolean shouldLeave() {
        return group.isValid() && config.WHITELIST_TAG != null && config.LEAVE_NO_WHITELISTED &&
                group.members.stream()
                        .map(m -> main.config.PLAYER_INFOS.get(m.id))
                        .noneMatch(i -> i != null && i.hasTag(config.WHITELIST_TAG));
    }

    public void acceptInvite(Invite inv) {
        int idx = invites.indexOf(inv);
        if (idx >= 0)
            clickBtn(MARGIN_WIDTH + INVITE_WIDTH, 0, HEADER_HEIGHT + BUTTON_HEIGHT, idx);
    }

    public void sendInvite(String username) {
        sendInvite(username, 60_000);
    }

    public void sendInvite(String username, long wait) {
        if (API.hasCapability(GameAPI.Capability.DIRECT_POST_ACTIONS)) {
            API.pasteText(username,
                    NativeAction.Mouse.CLICK.of(x + MARGIN_WIDTH + (INVITE_WIDTH / 2), y + getInvitingHeight()),
                    NativeAction.Mouse.CLICK.of(x + MARGIN_WIDTH + (INVITE_WIDTH / 2), y + getInvitingHeight()),
                    NativeAction.Mouse.CLICK.after(x + MARGIN_WIDTH + INVITE_WIDTH + (BUTTON_WIDTH / 2), y + getInvitingHeight()));
        } else {
            click(MARGIN_WIDTH + (INVITE_WIDTH / 2), getInvitingHeight());
            click(MARGIN_WIDTH + (INVITE_WIDTH / 2), getInvitingHeight());
//        Time.sleep(100); // This should not be here, but will stay for now
            API.sendText(username);
//        Time.sleep(500); // This should not be here, but will stay for now
            click(MARGIN_WIDTH + INVITE_WIDTH + (BUTTON_WIDTH / 2), getInvitingHeight());
        }
        pastInvites.put(username, System.currentTimeMillis() + wait); // Wait until re-invite
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

    @Override
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

    private int getInvitingHeight() {
        return HEADER_HEIGHT + (group.isValid() ? getGroupHeight() : BUTTON_HEIGHT) - (BUTTON_HEIGHT / 2);
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

        private final int notLeaderPos;

        GroupAction(int notLeaderPos) {
            this.notLeaderPos = notLeaderPos;
        }

        public int idx(boolean leader) {
            return leader ? ordinal() : notLeaderPos;
        }
    }

    @Override
    public boolean hasGroup() {
        return group.isValid();
    }

    @Override
    public int getId() {
        return group.id;
    }

    @Override
    public int getSize() {
        return group.size;
    }

    @Override
    public int getMaxSize() {
        return group.maxSize;
    }

    @Override
    public boolean isOpen() {
        return group.isOpen;
    }

    @Override
    public boolean isLeader() {
        return group.isLeader;
    }

    @Override
    public List<? extends eu.darkbot.api.game.group.GroupMember> getMembers() {
        return Collections.unmodifiableList(group.members);
    }

    @Override
    public eu.darkbot.api.game.group.@Nullable GroupMember getSelectedMember() {
        return group.selectedMember;
    }

    @Override
    public eu.darkbot.api.game.group.@Nullable GroupMember getMember(int id) {
        return group.getMember(id);
    }

    @Override
    public List<? extends eu.darkbot.api.game.group.GroupMember.Invite> getInvites() {
        return invites;
    }

    /*
    @Override
    public void kickMember(int id) {
        kick(id);
    }


    @Override
    public void acceptInvite(eu.darkbot.api.game.group.GroupMember.Invite invite) {
        acceptInvite((Invite) invite);
    }*/
}