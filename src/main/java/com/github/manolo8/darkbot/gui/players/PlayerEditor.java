package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.UnresolvedPlayer;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;

public class PlayerEditor extends JPanel {
    private JList<PlayerInfo> playerInfoList;
    private DefaultListModel<PlayerInfo> playersModel;
    protected Main main;

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 4, fill", "[][][grow][]", "[][grow]"));

        add(new AddPlayer(), "grow");
        add(new AddId(), "grow");
        add(new PlayerSearcher(this::refreshList), "grow");
        add(new PlayerTagEditor(this), "grow");

        playerInfoList = new JList<>(playersModel = new DefaultListModel<>());
        playerInfoList.setSelectionBackground(UIUtils.ACTION);
        playerInfoList.setCellRenderer(new PlayerRenderer());

        add(playerInfoList, "span 4, grow");
    }

    public void setup(Main main) {
        this.main = main;
        main.config.PLAYER_INFOS.values().forEach(playersModel::addElement);
        this.main.config.PLAYER_UPDATED.add(i -> refreshList(null));
    }

    public void refreshList(String filter) {
        if (main == null) return;
        playersModel.clear();
        main.config.PLAYER_INFOS
                .values()
                .stream()
                .filter(pi -> pi.filter(filter))
                .sorted(Comparator.comparing(pi -> pi.username))
                .forEach(playersModel::addElement);
    }

    public void addTagToPlayers(PlayerTag tag) {
        List<PlayerInfo> players = playerInfoList.getSelectedValuesList();

        if (players.isEmpty()) {
            Popups.showMessageAsync("Select players first", "To add tags to players, first select the players", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (tag == null) {
            tag = createTag();
            if (tag == null) return;
        }

        for (PlayerInfo p : players) {
            p.setTag(tag, null);
        }
        main.config.changed = true;
        playerInfoList.updateUI();
    }

    public void removeTagFromPlayers(PlayerTag tag) {
        if (tag == null) return;
        List<PlayerInfo> players = playerInfoList.getSelectedValuesList();

        if (players.isEmpty()) {
            Popups.showMessageAsync("Select players first", "To remove tags from players, first select the players", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (PlayerInfo p : players) p.removeTag(tag);
        main.config.changed = true;
        playerInfoList.updateUI();
    }

    public PlayerTag createTag() {
        PlayerTag tag;
        String name = JOptionPane.showInputDialog(this, "Tag name", "Add player tag", JOptionPane.QUESTION_MESSAGE);
        if (name == null) return null;
        Color color = JColorChooser.showDialog(this, "Tag color", null);
        if (color == null) return null;
        main.config.PLAYER_TAGS.put(name, tag = new PlayerTag(name, color));
        main.config.changed = true;
        return tag;
    }

    public void deleteTag(PlayerTag tag) {
        if (tag == null) return;
        int result = JOptionPane.showConfirmDialog(this,
                "Do you want to delete the " + tag.name + " tag, and remove it from everyone?",
                "Are you sure?",
                JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION)
            main.config.PLAYER_TAGS.remove(tag.name);

        for (PlayerInfo p : main.config.PLAYER_INFOS.values()) {
            p.removeTag(tag);
        }
        main.config.changed = true;
        playerInfoList.updateUI();
    }

    public class AddPlayer extends MainButton {
        public AddPlayer() {
            super(UIUtils.getIcon("add"), "User by name");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(this, "Username:", "Add player", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            main.config.UNRESOLVED.add(new UnresolvedPlayer(name));
        }
    }

    public class AddId extends MainButton {
        public AddId() {
            super(UIUtils.getIcon("add"), "User by id");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog(this, "User ID:", "Add player", JOptionPane.QUESTION_MESSAGE);
            if (id == null) return;
            try {
                main.config.UNRESOLVED.add(new UnresolvedPlayer(Integer.parseInt(id.trim())));
            } catch (NumberFormatException ex) {
                Popups.showMessageAsync("Invalid user ID", id + " is not a valid number", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
