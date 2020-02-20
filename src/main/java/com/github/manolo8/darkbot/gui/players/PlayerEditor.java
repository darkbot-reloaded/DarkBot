package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.UnresolvedPlayer;
import com.github.manolo8.darkbot.gui.components.MainButton;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PlayerEditor extends JPanel {

    private JButton addPlayer;
    private JButton addUserId;
    private JList<PlayerInfo> players;

    private DefaultListModel<PlayerInfo> playersModel;

    private Main main;

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 3, fill", "[grow][][]", "[][grow]"));
        initComponents();
        setComponentPosition();
    }

    private void initComponents() {
        this.addPlayer = new AddPlayer();
        this.addUserId = new AddId();
        this.players = new JList<>(playersModel = new DefaultListModel<>());
    }

    private void setComponentPosition() {
        add(new JLabel("Add new user by: "));
        add(addPlayer, "grow");
        add(addUserId, "grow");
        add(players, "span 4, grow");
    }

    public void setup(Main main) {
        this.main = main;
        main.config.PLAYER_INFOS.values().forEach(playersModel::addElement);
        this.main.config.PLAYER_UPDATED.add(i -> refreshList());
    }

    public void refreshList() {
        if (main == null) return;
        playersModel.clear();
        main.config.PLAYER_INFOS.values().forEach(playersModel::addElement);
    }

    public class AddPlayer extends MainButton {
        public AddPlayer() {
            super("Playername");
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
            super("User Id");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog(this, "User ID:", "Add player", JOptionPane.QUESTION_MESSAGE);
            if (id == null) return;
            main.config.UNRESOLVED.add(new UnresolvedPlayer(Integer.parseInt(id)));
        }
    }

}
