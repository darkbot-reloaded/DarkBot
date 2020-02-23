package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.tree.components.JLabel;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class PlayerTagEditor extends MainButton {
    private PlayerEditor editor;

    private JPopupMenu tags = new JPopupMenu("Add tags");
    private long keepClosed;

    private Map<PlayerTag, TagEntry> tagCache = new HashMap<>();

    public PlayerTagEditor(PlayerEditor editor) {
        super(UIUtils.getIcon("add"), "Player tag");
        this.editor = editor;

        tags.setBorder(UIUtils.getBorder());
        tags.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                keepClosed = System.currentTimeMillis() + 100;
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (keepClosed > System.currentTimeMillis()) {
            keepClosed = 0;
            return;
        }

        if (editor.main.config.PLAYER_TAGS.isEmpty()) {
            editor.addTagToPlayers(null);
            return;
        }

        tags.removeAll();
        for (PlayerTag tag : editor.main.config.PLAYER_TAGS.values()) {
            //tags.add(tagCache.computeIfAbsent(tag, TagEntry::new));
            tags.add(new TagEntry(tag));
        }

        tags.addSeparator();
        tags.add(tagCache.computeIfAbsent(null, TagEntry::new));

        tags.show(this, 0, getHeight() - 1);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        super.mousePressed(e);
        if (keepClosed > System.currentTimeMillis()) keepClosed = Long.MAX_VALUE;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        super.mouseExited(e);
        keepClosed = 0;
    }

    private class TagEntry extends JMenuItem {
        private static final int ALPHA = 96;
        private PlayerTag tag;

        TagEntry(PlayerTag tag) {
            super(tag == null ? "Add new tag" : " ");
            this.tag = tag;
            setOpaque(true);
            if (tag != null) {
                setBackground(UIUtils.blendColor(tag.color, ALPHA));
                setLayout(new MigLayout("ins 0, gap 0, fill", "[]5px![grow]"));
                add(new TagDeleteButton());
                add(new JLabel(tag.name), "grow");
            }

            this.addActionListener(a -> editor.addTagToPlayers(tag));
        }

        private class TagDeleteButton extends MainButton {
            public TagDeleteButton() {
                super(UIUtils.getIcon("close"));
                super.actionColor = UIUtils.RED;
                setMargin(new Insets(0, 0, 0, 0));
                setBorder(null);
            }
            public Insets getInsets() {
                return new Insets(0, 0, 0, 0);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                tags.setVisible(false);
                editor.deleteTag(tag);
            }
        }

    }
}
