package com.github.manolo8.darkbot.gui.utils.inspector;

import com.github.manolo8.darkbot.core.utils.ByteUtils;
import com.github.manolo8.darkbot.utils.debug.ObjectInspector;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.util.Objects;
import java.util.function.BiConsumer;


public class ObjectInspectorUI extends JFrame {

    public ObjectInspectorUI() {
        super("Object Inspector");
        setLayout(new BorderLayout());
        setSize(600, 600);

        JTree treeView = new InspectorTree(new DefaultTreeModel(new ObjectTreeNode(
                new ObjectInspector.Slot("", "", null, 0, 0),
                0, true)));

        JTextField addressField = new AddressField((name, addr) -> {
            DefaultTreeModel model = (DefaultTreeModel) treeView.getModel();
            model.setRoot(new ObjectTreeNode(
                    new ObjectInspector.Slot(name, "Object", null, 0, 8), addr, true));
        });

        setLayout(new BorderLayout());
        add(addressField, BorderLayout.NORTH);
        add(new JScrollPane(treeView), BorderLayout.CENTER);
    }

    private static class AddressField extends JTextField {

        public AddressField(BiConsumer<String, Long> onRootUpdate) {
            addActionListener(ae -> {
                try {
                    long object = Long.parseLong(getText(), 16);
                    String objectName = ByteUtils.readObjectName(object);

                    if (!Objects.equals(objectName, "ERROR")) {
                        onRootUpdate.accept(objectName, object);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid " + getText());
                }
            });
        }

    }

}