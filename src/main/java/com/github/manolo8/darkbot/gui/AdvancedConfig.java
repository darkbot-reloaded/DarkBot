package com.github.manolo8.darkbot.gui;

import com.bulenkov.darcula.ui.DarculaTreeUI;
import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.tree.ConfigTree;
import com.github.manolo8.darkbot.gui.tree.TreeEditor;
import com.github.manolo8.darkbot.gui.tree.TreeRenderer;
import com.github.manolo8.darkbot.gui.tree.components.JBoolField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JNumberField;
import com.github.manolo8.darkbot.gui.tree.components.JShipConfigField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.plaf.TreeUI;
import javax.swing.plaf.basic.BasicTreeUI;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class AdvancedConfig extends JPanel {

    public static final int ROW_HEIGHT = 18;
    public static final int HEADER_HEIGHT = 24;

    private Config config;
    private ConfigTree treeModel;

    public AdvancedConfig() {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
    }

    void setEditingConfig(Config config) {
        removeAll();
        this.config = config;
        add(setupUI());
    }

    public void setCustomConfig(String name, Object config) {
        treeModel.setCustom(name, config);
    }

    private JComponent setupUI() {
        JTree configTree = new JTree(this.treeModel = new ConfigTree(config));
        configTree.setUI(new DarculaTreeUI(){
            @Override
            protected int getRowX(int row, int depth) { // The UI overrides these, and forces 8px.
                return totalChildIndent * (depth + depthOffset);
            }
            @Override
            public int getRightChildIndent() {
                return rightChildIndent;
            }
        });
        configTree.setEditable(true);
        configTree.setFocusable(false);
        configTree.setRootVisible(false);
        configTree.setShowsRootHandles(true);
        configTree.setToggleClickCount(1);
        ((BasicTreeUI) configTree.getUI()).setLeftChildIndent(8);
        ((BasicTreeUI) configTree.getUI()).setRightChildIndent(10);
        configTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(configTree);

        TreeRenderer renderer = new TreeRenderer();
        configTree.setCellRenderer(renderer);
        TreeEditor editor = new TreeEditor(configTree, renderer);
        editor.addEditor(new JCharField(), Character.class);
        editor.addEditor(new JBoolField(), boolean.class);
        editor.addEditor(new JNumberField(), double.class, int.class);
        editor.addEditor(new JStringField(), String.class);
        editor.addEditor(new JShipConfigField(), Config.ShipConfig.class);

        configTree.setCellEditor(editor);

        treeModel.addTreeModelListener((SimpleTreeListener) e -> unfoldTopLevelTree(configTree));
        unfoldTopLevelTree(configTree);

        JScrollPane scrollPane = new JScrollPane(configTree);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private void unfoldTopLevelTree(JTree configTree) {
        for (int i = configTree.getRowCount() - 1; i >= 0; i--) configTree.expandRow(i);
    }

    public static Dimension forcePreferredHeight(Dimension preferred) {
        preferred.height = ROW_HEIGHT;
        return preferred;
    }

}
