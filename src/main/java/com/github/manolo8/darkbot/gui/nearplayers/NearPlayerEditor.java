package com.github.manolo8.darkbot.gui.nearplayers;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.PlayerInfo;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;

import static com.github.manolo8.darkbot.Main.API;

public class NearPlayerEditor extends JPanel {

    private final JList<PlayerInfo> nearPlayerInfoList;
    private final DefaultListModel<PlayerInfo> nearPlayersModel;
    protected Main main;

    public NearPlayerEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 0", "[][]"));

        JPanel panel = new JPanel(new MigLayout("ins 0"));
        JButton invite = new JButton("Invite");
        JButton save = new JButton("Save");
        nearPlayerInfoList = new JList<>(nearPlayersModel = new DefaultListModel<>());
        nearPlayerInfoList.setCellRenderer(new NearPlayerRenderer());

        panel.add(invite);
        panel.add(save);
        add(panel, "split 2, wrap, align center");
        add(new JScrollPane(nearPlayerInfoList,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "grow");


//        nearPlayersModel.add(0, new PlayerInfo("asd", 123));
//        nearPlayersModel.add(1, new PlayerInfo("asdd", 1233));
//        nearPlayersModel.add(2, new PlayerInfo("asddd", 12333));

        nearPlayerInfoList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting())
                if (nearPlayerInfoList.getSelectedValue() != null)
                    System.out.println("selected: " + nearPlayerInfoList.getSelectedValue().userId + " " + nearPlayerInfoList.getSelectedValue().username);
        });
        invite.addActionListener(e ->
        {
            //TODO: add direct invite function
            //new GroupManager(main).trySendInvite(nearPlayerInfoList.getSelectedValue().username);
        });
        save.addActionListener(e ->
        {
            //TODO: add save function to player tag list
        });
        refreshList();
    }


    public void refreshList() {
        Thread t1 = new Thread(() -> {
            while (true) {
                if (main != null) {
                    for (int i = 0; i < main.mapManager.entities.ships.size(); i++) {
                        int finalI = i;
                        if (Arrays.stream(nearPlayersModel.toArray()).filter(a -> a.toString().contains(main.mapManager.entities.ships.get(finalI).playerInfo.username)).findFirst().orElse(null) == null) {
                            if (API.readMemoryString(main.mapManager.entities.ships.get(i).address, 192, 144).trim().equals("pet")) {
                                System.out.println(main.mapManager.entities.ships.get(i).playerInfo.username + " is pet");
                            } else {
                                nearPlayersModel.addElement(new PlayerInfo(main.mapManager.entities.ships.get(i).playerInfo.username, main.mapManager.entities.ships.get(i).id));
                            }
                        }
                    }
                    boolean found = false;
                    for (int j = 0; j < nearPlayersModel.size(); j++) {
                        for (int i = 0; i < main.mapManager.entities.ships.size(); i++) {
                            if (nearPlayersModel.get(j).toString().contains(main.mapManager.entities.ships.get(i).toString()))
                                found = true;
                        }
                        if (!found) {
                            if (nearPlayerInfoList.getSelectedValue() == null)
                                nearPlayersModel.remove(j);
                            else if (!nearPlayerInfoList.getSelectedValue().equals(nearPlayersModel.get(j)))
                                nearPlayersModel.remove(j);
                        }
                    }
                    if (main.mapManager.entities.ships.size() == 0) {
                        nearPlayersModel.removeAllElements();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        t1.start();
    }

    public void setup(Main main) {
        this.main = main;
    }
}
