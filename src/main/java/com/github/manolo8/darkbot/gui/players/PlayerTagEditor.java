package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PlayerTagEditor extends JPanel {
    private PlayerEditor editor;

    private TagButton latestClicked;
    private JPopupMenu tags = new JPopupMenu("Manage tags");

    private Map<PlayerTag, TagEntry> tagCache = new HashMap<>();

    public PlayerTagEditor(PlayerEditor editor) {
        super(new MigLayout("ins 0, gap 0", "[][][]"));
        this.editor = editor;

        add(new TagButton(UIUtils.getIcon("add"), null, editor::addTagToPlayers, true, false));
        add(new TagButton(UIUtils.getIcon("remove"), null, editor::removeTagFromPlayers, false, false));
        add(new TagButton(UIUtils.getIcon("close"), "Manage tags", editor::deleteTag, false, true));

        tags.setBorder(UIUtils.getBorder());
        tags.addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                latestClicked.keepClosed = System.currentTimeMillis() + 100;
            }
        });
    }

    public void showPopup(TagButton clicked) {
        latestClicked = clicked;
        if (clicked.keepClosed > System.currentTimeMillis()) {
            clicked.keepClosed = 0;
            return;
        }

        if (editor.main.config.PLAYER_TAGS.isEmpty()) {
            editor.addTagToPlayers(null);
            return;
        }

        tags.removeAll();
        for (PlayerTag tag : editor.main.config.PLAYER_TAGS.values()) {
            tags.add(tagCache.computeIfAbsent(tag, TagEntry::new).setHandler(clicked));
        }

        tags.addSeparator();
        tags.add(tagCache.computeIfAbsent(null, TagEntry::new).setHandler(clicked));

        tags.show(clicked, 0, getHeight() - 1);
    }

    private class TagButton extends MainButton {
        private long keepClosed;
        private Consumer<PlayerTag> action;

        public TagButton(Icon icon, String text,
                         Consumer<PlayerTag> action,
                         boolean leftBorder, boolean rightBorder) {
            super(icon, text);
            this.action = action;
            setBorder(UIUtils.getPartialBorder(1, leftBorder ? 1 : 0, 1, rightBorder ? 1 : 0));
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

        @Override
        public void actionPerformed(ActionEvent e) {
            showPopup(this);
        }
    }

    private class TagEntry extends JMenuItem implements ActionListener {
        private static final int ALPHA = 96;
        private PlayerTag tag;
        private TagButton handler;

        TagEntry(PlayerTag tag) {
            super(tag == null ? "Add new tag" : tag.name);
            this.tag = tag;
            setOpaque(true);
            if (tag != null)
                setBackground(UIUtils.blendColor(tag.color, ALPHA));
            this.addActionListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            handler.action.accept(tag);
        }

        public TagEntry setHandler(TagButton handler) {
            this.handler = handler;
            return this;
        }

    }
}
