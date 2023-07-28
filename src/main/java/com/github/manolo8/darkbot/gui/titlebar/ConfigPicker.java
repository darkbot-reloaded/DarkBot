package com.github.manolo8.darkbot.gui.titlebar;

import com.formdev.flatlaf.ui.FlatComboBoxUI;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.gui.utils.ButtonListRenderer;
import com.github.manolo8.darkbot.gui.utils.Popups;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import com.github.manolo8.darkbot.utils.I18n;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.ComboPopup;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.file.Paths;

public class ConfigPicker extends JComboBox<String> {

    private Main main;
    private final ButtonListRenderer renderer;

    public ConfigPicker() {
        setUI(new TabComboBoxUI());

        renderer = new ButtonListRenderer(UIUtils.getIcon("remove"),
                this::onRemove,
                idx -> idx > 0 && getSelectedIndex() != idx);

        setRenderer(renderer);

        setMinimumSize(new Dimension(20, 0));
        setMaximumSize(new Dimension(140, Short.MAX_VALUE));
        setBackground(UIUtils.BACKGROUND);
        setBorder(null);

        ComboPopup popup = (ComboPopup) getAccessibleContext().getAccessibleChild(0);
        JList<?> list = popup.getList();
        list.addMouseListener(renderer);
        list.addMouseMotionListener(renderer);

        addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                setup(main);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });

        addActionListener(a -> {
            if (main != null) {
                if (renderer.isOnButton()) setSelectedItem(main.configManager.getConfigName());
                else main.configChange.send((String) getSelectedItem());
            }
        });
    }

    @Override
    public void setSelectedItem(Object str) {
        super.setSelectedItem(str);
        // Makes it so the box resizes to fit this string more appropriately
        setPrototypeDisplayValue((String) str);
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


    // An ui that more closely integrates ComboBox as if it was a tab
    private class TabComboBoxUI extends FlatComboBoxUI {

        private Color hoverColor;
        private MouseListener hoverListener;

        @Override
        protected void installDefaults() {
            super.installDefaults();

            this.buttonBackground = null;
            this.focusedBackground = UIManager.getColor("TabbedPane.focusColor");
            this.hoverColor = UIManager.getColor("TabbedPane.hoverColor");
            this.padding.top = this.padding.bottom = this.padding.left = this.padding.right = 0;
        }

        public Color getBackground(boolean enabled) {
            if (hover || arrowButton != null && arrowButton.getModel().isRollover()) return hoverColor;
            return super.getBackground(enabled);
        }

        @Override
        protected Dimension getDisplaySize() {
            Dimension size = super.getDisplaySize();

            // Add a bit of extra space for buttons/padding
            size.width += 4;
            return size;
        }

        @Override
        protected void installListeners() {
            super.installListeners();

            hoverListener = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    buttonFocusedBackground = hoverColor;
                    repaint();
                }

                @Override
                public void mouseExited( MouseEvent e ) {
                    hover = false;
                    buttonFocusedBackground = focusedBackground;
                    repaint();
                }

                @Override
                public void mousePressed( MouseEvent e ) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased( MouseEvent e ) {
                    pressed = false;
                    repaint();
                }
            };
            comboBox.addMouseListener(hoverListener);
        }

        @Override
        public void installComponents() {
            super.installComponents();

            if (arrowButton != null) arrowButton.addMouseListener(hoverListener);
        }

        @Override
        public void uninstallComponents() {
            super.uninstallComponents();
            if (arrowButton != null) arrowButton.removeMouseListener(hoverListener);
        }

        @Override
        protected void uninstallListeners() {
            super.uninstallListeners();

            comboBox.removeMouseListener(hoverListener);
            this.hoverListener = null;
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

                @Override
                protected Rectangle computePopupBounds( int px, int py, int pw, int ph ) {
                    // Use whole list when creating popup bounds
                    setPrototypeDisplayValue(null);
                    Rectangle rectangle = super.computePopupBounds( px, py, pw, ph );
                    // Extra space for the buttons, min width to fix "Add new" being wider
                    rectangle.width = Math.max(rectangle.width + 16, 100);
                    setPrototypeDisplayValue((String) getSelectedItem());
                    return rectangle;
                }
            };
        }
    }

}
