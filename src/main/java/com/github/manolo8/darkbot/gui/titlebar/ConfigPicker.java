package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.ui.FlatComboBoxUI;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.ButtonListRenderer;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.nio.file.Paths;

public class ConfigPicker extends JComboBox<String> {

    private Main main;
    private final ButtonListRenderer renderer;

    public ConfigPicker() {
        setUI(new FlatComboBoxUI() {
            @Override
            protected Dimension getDisplaySize() {
                Dimension size = super.getDisplaySize();
                size.width += 16;
                return size;
            }

            @Override
            protected ComboPopup createPopup() {
                return new FlatComboBoxUI.FlatComboPopup(ConfigPicker.this) {
                    @Override protected void configurePopup() {
                        super.configurePopup();
                        setOpaque(true);
                        add(new JSeparator());
                        JMenuItem item = new JMenuItem("Add new", UIUtils.getIcon("add"));
                        item.addActionListener(a -> ConfigPicker.this.onAdd());
                        add(item);
                    }
                };
            }
        });

        renderer = new ButtonListRenderer(UIUtils.getIcon("remove"),
                this::onRemove,
                idx -> idx > 0 && getSelectedIndex() != idx);

        setRenderer(renderer);

        setMinimumSize(new Dimension(40, 0));
        setPreferredSize(new Dimension(100, 0));
        setMaximumSize(new Dimension(140, Short.MAX_VALUE));
        setBorder(BorderFactory.createEmptyBorder());

        ComboPopup popup = (ComboPopup) getAccessibleContext().getAccessibleChild(0);
        JList<?> list = popup.getList();
        list.addMouseListener(renderer);
        list.addMouseMotionListener(renderer);

        addActionListener(a -> {
            if (main != null) {
                if (renderer.isOnButton()) setSelectedItem(main.configManager.getConfigName());
                else main.configChange.send((String) getSelectedItem());
            }
        });
    }

    public void setup(Main main) {
        this.main = null; // Ensure no updates happen
        removeAllItems();
        main.configManager.getAvailableConfigs().forEach(this::addItem);
        setSelectedItem(main.configManager.getConfigName());
        this.main = main;
    }

    public void onRemove(int idx) {
        int result = JOptionPane.showConfirmDialog(this,
                I18n.get("configs.delete_menu.msg", ConfigPicker.this.getModel().getElementAt(idx)),
                I18n.get("configs.delete_menu.title"),
                JOptionPane.YES_NO_OPTION);
        if (result != JOptionPane.YES_OPTION) return;

        MutableComboBoxModel<String> model = (MutableComboBoxModel<String>) ConfigPicker.this.getModel();
        String config = model.getElementAt(idx);
        if (main.configManager.deleteConfig(config)) {
            model.removeElementAt(idx);
        } else {
            Popups.of(
                    "Error deleting config",
                    "Error deleting the config, check the logs for more info.",
                    JOptionPane.ERROR_MESSAGE).showAsync();
        }
        showPopup();
    }

    public void onAdd() {
        JPanel panel = new JPanel(new MigLayout("ins 0, wrap 2", "[]5px[grow]"));

        JTextField name = new JTextField(10);
        panel.add(new JLabel(I18n.get("configs.add_menu.name")));
        panel.add(name, "grow");

        JComboBox<String> copy = new JComboBox<>();
        copy.addItem(I18n.get("configs.add_menu.copy.empty"));
        main.configManager.getAvailableConfigs().forEach(copy::addItem);
        panel.add(new JLabel(I18n.get("configs.add_menu.copy")));
        panel.add(copy, "grow");

        JButton createConfig = new JButton(I18n.get("configs.add_menu.create"));
        createConfig.addActionListener(event -> SwingUtilities.getWindowAncestor(panel).setVisible(false));

        JOptionPane pane = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION, null, new Object[]{createConfig}, createConfig);
        JDialog dialog = pane.createDialog(this, I18n.get("configs.add_menu.create"));

        pane.selectInitialValue();
        dialog.setVisible(true);
        dialog.dispose();

        if (pane.getValue() == null || name.getText().trim().isEmpty()) return;

        if (main.configManager.createNewConfig(name.getText(),
                copy.getSelectedIndex() == 0 ? null : (String) copy.getSelectedItem())) {
            setup(main);

            main.setConfig(Paths.get(name.getText()).getFileName().toString());
        } else {
            Popups.of(
                    "Error creating config",
                    "Error creating the config, check the logs for more info.",
                    JOptionPane.ERROR_MESSAGE).showAsync();
        }
    }

}
