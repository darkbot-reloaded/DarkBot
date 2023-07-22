package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.gui.MainGui;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;


public class ObjectInspectorUI extends JFrame {

    public ObjectInspectorUI() {
        super("Object Inspector");
        setLayout(new MigLayout());
        setSize(600, 600);
        setIconImage(MainGui.ICON);

        InspectorTree treeView = new InspectorTree(new DefaultTreeModel(new ObjectTreeNode(
                new ObjectInspector.Slot("", "", null, 0, 0),
                () -> 0L, true)));

        JComboBox<AddressEntry> addressCombo = new AddressCombo((name, addr) -> {
            DefaultTreeModel model = (DefaultTreeModel) treeView.getModel();
            model.setRoot(new ObjectTreeNode(
                    new ObjectInspector.Slot(name, "Object", null, 0, 8), addr, true));
        });

        JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(250, 10, 1000, 10));
        delaySpinner.addChangeListener(l -> treeView.setTimerDelay((Integer) delaySpinner.getValue()));

        add(addressCombo, "grow");
        add(delaySpinner, "wrap");
        add(new JScrollPane(treeView), "push, grow, span");
    }

    private static class AddressCombo extends JComboBox<AddressEntry> {

        public AddressCombo(BiConsumer<String, Supplier<Long>> onRootUpdate) {
            addActionListener(ae -> {
                Long object;
                Object selectedItem = getSelectedItem();
                if (selectedItem instanceof AddressEntry) {
                    object = ((AddressEntry) selectedItem).address.get();
                } else {
                    object = tryParse((String) selectedItem, true);
                    if (object == null) tryParse((String) selectedItem, false);
                }

                if (object != null) {
                    String objectName = ByteUtils.readObjectName(object);

                    if (!Objects.equals(objectName, "ERROR")) {
                        onRootUpdate.accept(objectName, () -> object);
                    }
                }
            });

            setEditable(true);

            BotInstaller b = HeroManager.instance.main.botInstaller;
            addItem(new AddressEntry("GuiManager", b.guiManagerAddress::get));
            addItem(new AddressEntry("ScreenManager", b.screenManagerAddress::get));
            addItem(new AddressEntry("ConnectionManager", b.connectionManagerAddress::get));
            addItem(new AddressEntry("Main Address", b.mainAddress::get));
            addItem(new AddressEntry("MainApp Address", b.mainApplicationAddress::get));
            addItem(new AddressEntry("Hero Address", () -> HeroManager.instance.address));
            addItem(new AddressEntry("HeroInfo Address", b.heroInfoAddress::get));
            addItem(new AddressEntry("Settings Address", b.settingsAddress::get));
        }

        private Long tryParse(String text, boolean hex) {
            try {
                return Long.parseLong(text, hex ? 16 : 10);
            } catch (NumberFormatException e) {
                System.out.println("Invalid " + (hex ? "hex " : "") + getSelectedItem());
            }
            return null;
        }
    }

    private static class AddressEntry {
        private final String name;
        private final Supplier<Long> address;

        private AddressEntry(String name, Supplier<Long> address) {
            this.name = name;
            this.address = address;
        }

        @Override
        public String toString() {
            Long value = address.get();
            if (value == null)
                return name;
            return String.format("%s - 0x%x", name, value);
        }
    }

}