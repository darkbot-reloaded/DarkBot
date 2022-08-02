package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.UnresolvedPlayer;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.PopupButton;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class PlayerManager extends JPanel {

    private final PlayerEditor editor;

    public PlayerManager(PlayerEditor editor) {
        super(new MigLayout("ins 0, gap 0", "[][]"));
        this.editor = editor;

        add(new AddPlayer(), "grow");
        add(new RemovePlayers(), "grow");
    }

    public class AddPlayer extends PopupButton<JPopupMenu> {

        public AddPlayer() {
            super(UIUtils.getIcon("add"), I18n.get("players.add_player"), new JPopupMenu());

            popup.add(create(UIUtils.getIcon("add"), I18n.get("players.add_player.by_id"), this::addPlayerById));
            popup.add(create(UIUtils.getIcon("add"), I18n.get("players.add_player.by_name"), this::addPlayerByUsername));
            popup.add(create(UIUtils.getIcon("add"), I18n.get("players.add_player.selected"), this::addSelectedPlayer));

            setBorder(UIUtils.getPartialBorder(1, 1, 1, 0));
        }

        private JMenuItem create(Icon icon, String name, Runnable listener) {
            JMenuItem item = new JMenuItem(name, icon);
            item.addActionListener(e -> listener.run());
            return item;
        }

        private void addPlayerById() {
            String id = JOptionPane.showInputDialog(this, I18n.get("players.add_player.user_id"),
                    I18n.get("players.add_player"), JOptionPane.QUESTION_MESSAGE);
            if (id == null) return;
            try {
                editor.main.config.UNRESOLVED.add(new UnresolvedPlayer(Integer.parseInt(id.trim())));
            } catch (NumberFormatException ex) {
                Popups.of(I18n.get("players.add_player.invalid_id"),
                        I18n.get("players.add_player.invalid_id.no_number", id), JOptionPane.ERROR_MESSAGE).showAsync();
            }
        }

        private void addPlayerByUsername() {
            String name = JOptionPane.showInputDialog(this, I18n.get("players.add_player.username"),
                    I18n.get("players.add_player"), JOptionPane.QUESTION_MESSAGE);
            if (name == null) return;
            editor.main.config.UNRESOLVED.add(new UnresolvedPlayer(name));
        }

        private void addSelectedPlayer() {
            if (editor.getPlayerInfoList().isSelectionEmpty()) {
                Popups.of(I18n.get("players.add_player.selected.invalid_selection"),
                        I18n.get("players.add_player.selected.invalid_selection.no_selected"), JOptionPane.ERROR_MESSAGE).showAsync();
                return;
            }

            for (PlayerInfo pl : editor.getPlayerInfoList().getSelectedValuesList()) {
                editor.main.config.PLAYER_INFOS.put(pl.getUserId(), pl);
                editor.getNearbyPlayers().remove(pl);
            }
            editor.refreshList();
        }
    }

    public class RemovePlayers extends MainButton {
        public RemovePlayers() {
            super(UIUtils.getIcon("remove"));
            setBorder(UIUtils.getPartialBorder(1, 0, 1, 1));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            editor.removePlayers();
        }
    }

}
