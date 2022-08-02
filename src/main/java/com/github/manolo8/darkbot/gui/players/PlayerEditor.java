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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class PlayerEditor extends JPanel implements Listener {
    private final JList<PlayerInfo> playerInfoList;
    private final DefaultListModel<PlayerInfo> playersModel;
    private final PlayerRenderer renderer;
    private final Set<PlayerInfo> nearbyPlayers = new LinkedHashSet<>();

    private final PlayerManager playerManager;
    private final SearchField sf;
    private final PlayerTagManager tagEditor;
    protected Main main;
    private boolean registered;

    public PlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 3, fill", "[][grow][]", "[][grow,fill]"));

        add(playerManager = new PlayerManager(this), "grow");
        add(sf = new SearchField(this::refreshList), "grow");
        add(tagEditor = new PlayerTagManager(this), "grow");

        playerInfoList = new JList<>(playersModel = new DefaultListModel<>());
        playerInfoList.setCellRenderer(renderer = new PlayerRenderer(playerInfoList));

        add(new JScrollPane(playerInfoList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "span, grow");

        playerInfoList.addListSelectionListener((event) -> {
            if (event.getValueIsAdjusting()) return;
            int min = event.getFirstIndex();
            int max = Math.min(event.getLastIndex() + 1, playersModel.size());
            for (int i = min; i < max; i++) {
                PlayerInfo pl = playersModel.get(i);
                if (!playerInfoList.isSelectedIndex(i) &&
                        !main.config.PLAYER_INFOS.containsKey(pl.getUserId()) &&
                        !nearbyPlayers.contains(pl)) {
                    playersModel.removeElementAt(i--);
                    max--;
                }
            }
        });
    }

    public void setup(Main main) {
        this.main = main;

        tagEditor.setup(main);

        if (!registered) {
            registered = true;
            EventBrokerAPI eventBroker = main.pluginAPI.requireAPI(EventBrokerAPI.class);
            eventBroker.registerListener(this);
        }

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
        renderer.setAddedPlayerCount(playersModel.getSize());
        for (PlayerInfo p : nearbyPlayers) {
            playersModel.addElement(p);
        }
    }

    @EventHandler
    public void onEntityPlayerAdd(EntitiesAPI.EntityCreateEvent event) {
        if (event.getEntity() instanceof Player
                && playersModel != null
                && main.config.PLAYER_INFOS != null
                && !main.config.PLAYER_INFOS.containsKey(event.getEntity().getId())) {

            SwingUtilities.invokeLater(() -> {
                int idx = indexOfPlayersModel(event.getEntity().getId());
                if (idx < 0) {
                    PlayerInfo playerInfo = new PlayerInfo((Player) event.getEntity());
                    nearbyPlayers.add(playerInfo);
                    playersModel.addElement(playerInfo);
                } else {
                    nearbyPlayers.add(playersModel.get(idx));
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
                int idx = indexOfPlayersModel(event.getEntity().getId());
                if (idx == -1) return;
                PlayerInfo info = playersModel.getElementAt(idx);
                if (!playerInfoList.isSelectedIndex(idx)) {
                    nearbyPlayers.remove(info);
                    playersModel.removeElement(info);
                } else {
                    nearbyPlayers.remove(info);
                }
            });
        }
    }

    public int indexOfPlayersModel(int playerId) {
        for (int i = 0; i < playersModel.size(); i++)
            if (playerId == playersModel.get(i).getUserId()) return i;
        return -1;
    }

    public void addTagToPlayers(PlayerTag tag) {
        List<PlayerInfo> players = playerInfoList.getSelectedValuesList();

        if (tag == null) {
            tag = PlayerTagUtils.createTag(this);
            if (tag == null) return;
            main.config.PLAYER_TAGS.add(tag);
        } else if (players.isEmpty()) {
            Popups.of(I18n.get("players.select_players.warn.title"),
                    I18n.get("players.select_players.warn.add"), JOptionPane.INFORMATION_MESSAGE).showAsync();
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
            Popups.of(I18n.get("players.select_players.warn.title"),
                    I18n.get("players.select_players.warn.remove"), JOptionPane.INFORMATION_MESSAGE).showAsync();
            return;
        }
        for (PlayerInfo p : players) p.removeTag(tag);
        ConfigEntity.changed();
        playerInfoList.updateUI();
    }

    public void deleteTag(PlayerTag tag) {
        if (tag == null) return;
        int result = Popups.of(I18n.get("players.delete_tag.confirm.title"),
                        I18n.get("players.delete_tag.confirm.msg", tag.name), JOptionPane.QUESTION_MESSAGE)
                .optionType(JOptionPane.YES_NO_OPTION)
                .parent(this)
                .showOptionSync();
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
            Popups.of(I18n.get("players.select_players.warn.title"),
                    I18n.get("players.select_players.warn.remove_player"), JOptionPane.INFORMATION_MESSAGE).showAsync();
            return;
        }

        int result = Popups.of(I18n.get("players.remove_player.title"),
                        I18n.get("players.remove_player.msg", players.size()), JOptionPane.QUESTION_MESSAGE)
                .optionType(JOptionPane.YES_NO_OPTION)
                .parent(this)
                .showOptionSync();

        if (result != JOptionPane.YES_OPTION) return;

        for (PlayerInfo player : players) {
            main.config.PLAYER_INFOS.remove(player.userId);
            playersModel.removeElement(player);
            nearbyPlayers.remove(player);
        }
        ConfigEntity.changed();
        refreshList();
    }

    public Set<PlayerInfo> getNearbyPlayers() {
        return nearbyPlayers;
    }

    public JList<PlayerInfo> getPlayerInfoList() {
        return playerInfoList;
    }
}