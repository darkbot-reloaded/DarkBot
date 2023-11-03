package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.BotInstaller;
import com.github.manolo8.darkbot.core.manager.HeroManager;
import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.gui.MainGui;
import net.miginfocom.swing.MigLayout;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;


public class ObjectInspectorUI extends JFrame {

    public ObjectInspectorUI(JMenuItem menuItem) {
        super("Object Inspector");
        setLayout(new MigLayout("ins 0, gap 0"));
        setSize(600, 600);
        setIconImage(MainGui.ICON);
        setLocationRelativeTo(null);
        setJMenuBar(new InspectorTitleBar(this));
        DefaultTreeModel treeModel = new DefaultTreeModel(ObjectTreeNode.root("-", () -> 0L));

        Object paintLines = UIManager.put("Tree.paintLines", true);
        InspectorTree treeView = new InspectorTree(treeModel);
        UIManager.put("Tree.paintLines", paintLines);

        JComboBox<AddressEntry> addressCombo = new AddressCombo((name, addr) -> {
            ObjectTreeNode root = ObjectTreeNode.root(name, addr);
            root.loadChildren(treeView);
            treeModel.setRoot(root);
        });

        JSpinner delaySpinner = new JSpinner(new SpinnerNumberModel(250, 10, 5_000, 50));
        delaySpinner.setEditor(new JSpinner.NumberEditor(delaySpinner, "0ms"));
        delaySpinner.addChangeListener(l -> treeView.setTimerDelay((Integer) delaySpinner.getValue()));

        add(addressCombo, "grow");
        add(delaySpinner, "wrap");
        add(new JScrollPane(treeView), "push, grow, span");

        menuItem.setEnabled(false);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                menuItem.setEnabled(true);
            }
        });
    }

    private static class AddressCombo extends JComboBox<AddressEntry> {

        public AddressCombo(BiConsumer<String, LongSupplier> onRootUpdate) {
            putClientProperty("FlatLaf.style", Map.of("padding", new Insets(0, 5, 0, 5)));

            addActionListener(ae -> {
                LongSupplier addrSupplier = getSelectedAddr();
                long addr;

                if (addrSupplier == null || (addr = addrSupplier.getAsLong()) == 0) {
                    putClientProperty("JComponent.outline", "error");
                    return;
                } else {
                    putClientProperty("JComponent.outline", null);
                }

                String objectName = ByteUtils.readObjectNameDirect(addr);
                if (!Objects.equals(objectName, "ERROR")) {
                    onRootUpdate.accept(objectName, addrSupplier);
                }
            });

            setEditable(true);

            BotInstaller b = HeroManager.instance.main.pluginAPI.requireInstance(BotInstaller.class);
            addItem(new AddressEntry("GuiManager", b.guiManagerAddress::get));
            addItem(new AddressEntry("ScreenManager", b.screenManagerAddress::get));
            addItem(new AddressEntry("ConnectionManager", b.connectionManagerAddress::get));
            addItem(new AddressEntry("Main Address", b.mainAddress::get));
            addItem(new AddressEntry("MainApp Address", b.mainApplicationAddress::get));
            addItem(new AddressEntry("Hero Address", () -> HeroManager.instance.address));
            addItem(new AddressEntry("HeroInfo Address", b.heroInfoAddress::get));
            addItem(new AddressEntry("Settings Address", b.settingsAddress::get));
        }

        private LongSupplier getSelectedAddr() {
            Object selectedItem = getSelectedItem();
            if (selectedItem instanceof AddressEntry) {
                return () -> {
                    Long l = ((AddressEntry) selectedItem).address.get();
                    return l == null ? 0 : l;
                };
            } else if (selectedItem instanceof String) {
                Long addr = parseAddress((String) selectedItem);
                if (addr != null) return () -> addr;
            } else {
                System.out.println("Unknown type of value in address combo: " + selectedItem);
            }
            return null;
        }
    }

    public static Long parseAddress(String text) {
        try {
            if (text.startsWith("0x"))
                return Long.parseLong(text.substring(2), 16);
            return Long.parseLong(text, 10);
        } catch (NumberFormatException e) {
            System.out.println("Invalid address '" + text + "': " + e.getMessage());
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
            if (value == null || value == 0) return name + " - Unknown address";
            return String.format("%s - 0x%x", name, value);
        }
    }

}