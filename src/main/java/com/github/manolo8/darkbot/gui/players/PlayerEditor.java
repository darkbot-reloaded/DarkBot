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

public class PlayerEditor extends JPanel {

    private DefaultListModel<PlayerInfo> playersModel;
    private Main main;

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 4, fill", "[][][grow][]", "[][grow]"));

        add(new AddPlayer(), "grow");
        add(new AddId(), "grow");
        add(new PlayerSearcher(this::refreshList), "grow");
        add(new AddTag(), "grow");
        add(createPlayerList(), "span 4, grow");
    }

    private JList<PlayerInfo> createPlayerList() {
        JList<PlayerInfo> players = new JList<>(playersModel = new DefaultListModel<>());
        players.setSelectionBackground(UIUtils.ACTION);
        players.setCellRenderer(new PlayerRenderer());
        return players;
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

    public class AddTag extends MainButton {
        public AddTag() {
            super(UIUtils.getIcon("add"), "Player tag");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(this, "Tag name", "Add player tag", JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            Color color = JColorChooser.showDialog(this, "Tag color", null);
            if (color == null) return;
            main.config.PLAYER_TAGS.put(name, new PlayerTag(name, color));
        }
    }

}
