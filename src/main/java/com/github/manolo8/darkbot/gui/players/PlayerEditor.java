package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PlayerEditor extends JPanel {

    private JButton addPlayer;
    private JButton addUserId;
    private JButton refresh;
    private JList<PlayerInfo> players;

    private DefaultListModel<PlayerInfo> playersModel;

    private Main main;

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 4, fill", "[grow][][][]", "[][grow]"));
        initComponents();
        setComponentPosition();
    }

    private void initComponents() {
        add(new JLabel("Add new user by: "));
        this.addPlayer = new AddPlayer();
        this.addUserId = new AddId();
        this.refresh = new ReloadPlayers();
        this.players = new JList<>(playersModel = new DefaultListModel<>());
    }

    private void setComponentPosition() {
        add(addPlayer, "grow");
        add(addUserId, "grow");
        add(refresh, "grow");
        add(players, "span 4");
    }

    public void setup(Main main) {
        this.main = main;

        refreshList();
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
            main.config.UNRESOLVED.add(new PlayerInfo(name));
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
            main.config.UNRESOLVED.add(new PlayerInfo(Integer.parseInt(id)));
        }
    }

    public class ReloadPlayers extends MainButton {
        public ReloadPlayers() {
            super(UIUtils.getIcon("reload"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            refreshList();
        }
    }
}
