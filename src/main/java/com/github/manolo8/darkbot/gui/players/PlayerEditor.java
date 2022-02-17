package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.PlayerInfo;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.SearchField;
import com.github.manolo8.darkbot.utils.I18n;
import eu.darkbot.api.events.EventHandler;
import eu.darkbot.api.events.Listener;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.EventBrokerAPI;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class PlayerEditor extends JPanel implements Listener {
    private final JList<PlayerInfo> playerInfoList;
    private final DefaultListModel<PlayerInfo> playersModel;
    public static final List<PlayerInfo> nearbyPlayerList = new ArrayList<>();

    private final PlayerManager playerManager;
    private final SearchField sf;
    private final PlayerTagManager tagEditor;
    protected Main main;

    public int indexOfPlayersModel(int playerId) {
        for (int i = 0; i < playersModel.size(); i++)
            if (playerId == playersModel.get(i).getUserId()) return i;
        return -1;
    }

    @EventHandler
    public void onEntityPlayerAdd(EntitiesAPI.EntityCreateEvent event) {
        if (event.getEntity() instanceof Player
                && playersModel != null
                && main.config.PLAYER_INFOS != null
                && !main.config.PLAYER_INFOS.containsKey(event.getEntity().getId())) {

            SwingUtilities.invokeLater(() -> {
                PlayerInfo playerInfo = new PlayerInfo((Player) event.getEntity());
                if (indexOfPlayersModel(event.getEntity().getId()) == -1) {
                    nearbyPlayerList.add(playerInfo);
                    playersModel.addElement(playerInfo);
                }
                if (playerInfoList.getSelectedValuesList().toString().contains(String.valueOf(playerInfo.userId)) && !nearbyPlayerList.toString().contains(String.valueOf(playerInfo.userId))){
                    for (int i = 0; i < playersModel.size(); i++) {
                        if (playersModel.get(i).toString().contains(playerInfo.toString())) {
                            nearbyPlayerList.add(playersModel.get(i));
                            break;
                        }
                    }
                }
            });
        }
    }

    @EventHandler
    public void onEntityPlayerRemove(EntitiesAPI.EntityRemoveEvent event) {
        if (event.getEntity() instanceof Player
                && playersModel != null
                && main.config.PLAYER_INFOS != null
                && !main.config.PLAYER_INFOS.containsKey(event.getEntity().getId())) {

            SwingUtilities.invokeLater(() -> {
                /*int idx = indexOf(event.getEntity().getId());
                if (idx == -1 || playerInfoList.isSelectedIndex(idx)) return;
                PlayerInfo info = playersModel.getElementAt(idx);
                nearbyPlayerList.remove(info);
                playersModel.removeElement(info);*/
                for (int i = 0; i < playersModel.size(); i++) {
                    PlayerInfo info = playersModel.getElementAt(i);
                    if (info.getUserId() == event.getEntity().getId()) {
                        if (!playerInfoList.isSelectedIndex(i)) {
                            nearbyPlayerList.remove(info);
                            playersModel.removeElement(info);
                            break;
                        } else {
                            nearbyPlayerList.remove(info);
                            break;
                        }
                    }
                }
            });
        }
    }

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 3, fill", "[][grow][]", "[][grow,fill]"));

        add(playerManager = new PlayerManager(this), "grow");
        add(sf = new SearchField(this::refreshList), "grow");
        add(tagEditor = new PlayerTagManager(this), "grow");

        playerInfoList = new JList<>(playersModel = new DefaultListModel<>());
        playerInfoList.setCellRenderer(new PlayerRenderer());

        add(new JScrollPane(playerInfoList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "span, grow");

        playerInfoList.addListSelectionListener(event -> SwingUtilities.invokeLater(() -> {
            if (!event.getValueIsAdjusting()) {

                int min = Math.max(event.getFirstIndex(), main.config.PLAYER_INFOS.size());
                int max = Math.min(event.getLastIndex() + 1, playersModel.size());
                for (int i = min; i < max; i++) {
                    PlayerInfo pl = playersModel.get(i);
                    if (!playerInfoList.isSelectedIndex(i) &&
                            !main.config.PLAYER_INFOS.containsKey(pl.getUserId()) &&
                            !nearbyPlayerList.toString().contains(pl.toString())) {
                        playersModel.removeElementAt(i--);
                        max--;
                    }
                }
                /*
                List<Integer> listToRemove = new ArrayList<>();
                for (int i = 0; i < playersModel.size() - main.config.PLAYER_INFOS.size(); i++) {
                    if (!nearbyPlayerList.contains(playersModel.get(i + main.config.PLAYER_INFOS.size()))) {
                        if (!playerInfoList.getSelectedValuesList().contains(playersModel.get(i + main.config.PLAYER_INFOS.size()))) {
                            listToRemove.add(i + main.config.PLAYER_INFOS.size());
                        }
                    }
                }
                Collections.reverse(listToRemove);
                for (int p : listToRemove) {
                    playersModel.remove(p);
                }*/

            }
        }));
    }

    public void setup(Main main) {
        this.main = main;

        tagEditor.setup(main);

        EventBrokerAPI eventBroker = main.pluginAPI.requireAPI(EventBrokerAPI.class);
        eventBroker.registerListener(this);

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
        for (PlayerInfo p : nearbyPlayerList) {
            playersModel.addElement(p);
        }
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

}
