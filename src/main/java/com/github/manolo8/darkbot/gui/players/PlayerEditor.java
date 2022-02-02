package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.UnresolvedPlayer;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.nearplayers.NearPlayerEditor;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PlayerEditor extends JPanel {
    private JList<PlayerInfo> playerInfoList;
    private DefaultListModel<PlayerInfo> playersModel;
    private SearchField sf;
    private PlayerTagEditor tagEditor;
    protected Main main;
    NearPlayerEditor nearPlayerEditor = new NearPlayerEditor();

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 5, fill", "[][][][grow][][]", "[][grow,fill]"));

        add(new AddPlayer(), "grow");
        add(new AddId(), "grow");
        add(new RemovePlayers(), "grow");
        add(sf = new SearchField(this::refreshList), "grow");
        add(tagEditor = new PlayerTagEditor(this), "grow");

        playerInfoList = new JList<>(playersModel = new DefaultListModel<>());
        playerInfoList.setCellRenderer(new PlayerRenderer());

        add(new JScrollPane(playerInfoList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "span 5, grow");
        add(nearPlayerEditor, "grow, cell 5 0 1 2"); //cell column row width height
    }

    public void setup(Main main) {
        this.main = main;
        nearPlayerEditor.setup(main);

        tagEditor.setup(main);

        refreshList();
        main.config.PLAYER_UPDATED.add(i -> SwingUtilities.invokeLater(this::refreshList));
    }

    public void refreshList() {
        refreshList(sf.getText());
    }

    public void refreshList(String filter) {
        if (main == null) return;
        String query = filter == null ? null : filter.toLowerCase(Locale.ROOT);
        playersModel.clear();
        main.config.PLAYER_INFOS
                .values()
                .stream()
                .filter(pi -> pi.filter(query))
                .sorted(Comparator.comparing(pi -> pi.username))
                .forEach(playersModel::addElement);
    }

    public void addTagToPlayers(PlayerTag tag) {
        List<PlayerInfo> players = playerInfoList.getSelectedValuesList();

        if (tag == null) {
            tag = PlayerTagUtils.createTag(this);
            if (tag == null) return;
            main.config.PLAYER_TAGS.add(tag);
        } else if (players.isEmpty()) {
            Popups.showMessageAsync(I18n.get("players.select_players.warn.title"), I18n.get("players.select_players.warn.add"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        for (PlayerInfo p : players) p.setTag(tag, null);
        ConfigEntity.changed();
        playerInfoList.updateUI();
    }

    public void removeTagFromPlayers(PlayerTag tag) {
        if (tag == null) return;
        List<PlayerInfo> players = playerInfoList.getSelectedValuesList();

        if (players.isEmpty()) {
            Popups.showMessageAsync(I18n.get("players.select_players.warn.title"), I18n.get("players.select_players.warn.remove"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        for (PlayerInfo p : players) p.removeTag(tag);
        ConfigEntity.changed();
        playerInfoList.updateUI();
    }

    public void deleteTag(PlayerTag tag) {
        if (tag == null) return;
        int result = JOptionPane.showConfirmDialog(this,
                I18n.get("players.delete_tag.confirm.msg", tag.name),
                I18n.get("players.delete_tag.confirm.title"),
                JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        main.config.PLAYER_TAGS.remove(tag);
        for (PlayerInfo p : main.config.PLAYER_INFOS.values()) {
            p.removeTag(tag);
        }
        ConfigEntity.changed();
        playerInfoList.updateUI();
    }

    public void removePlayers() {
        List<PlayerInfo> players = playerInfoList.getSelectedValuesList();
        if (players.isEmpty()) {
            Popups.showMessageAsync(I18n.get("players.select_players.warn.title"), I18n.get("players.select_players.warn.remove_player"), JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                I18n.get("players.remove_player.msg", players.size()),
                I18n.get("players.remove_player.title"),
                JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        for (PlayerInfo player : players) {
            main.config.PLAYER_INFOS.remove(player.userId);
            playersModel.removeElement(player);
        }
        ConfigEntity.changed();
    }

    public class AddPlayer extends MainButton {
        public AddPlayer() {
            super(UIUtils.getIcon("add"), I18n.get("players.add_player.by_name"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String name = JOptionPane.showInputDialog(this, I18n.get("players.add_player.username"),
                    I18n.get("players.add_player"), JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            main.config.UNRESOLVED.add(new UnresolvedPlayer(name));
        }
    }

    public class AddId extends MainButton {
        public AddId() {
            super(UIUtils.getIcon("add"), I18n.get("players.add_player.by_id"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String id = JOptionPane.showInputDialog(this, I18n.get("players.add_player.user_id"),
                    I18n.get("players.add_player"), JOptionPane.QUESTION_MESSAGE);
            if (id == null) return;
            try {
                main.config.UNRESOLVED.add(new UnresolvedPlayer(Integer.parseInt(id.trim())));
            } catch (NumberFormatException ex) {
                Popups.showMessageAsync(I18n.get("players.add_player.invalid_id"),
                        I18n.get("players.add_player.invalid_id.no_number", id), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public class RemovePlayers extends MainButton {
        public RemovePlayers() {
            super(UIUtils.getIcon("remove"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removePlayers();
        }
    }

}
