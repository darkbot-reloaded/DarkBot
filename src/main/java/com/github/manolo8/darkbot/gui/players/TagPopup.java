package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TagPopup extends JPopupMenu {

    private Collection<PlayerTag> playerTags;
    private Consumer<PlayerTag> callback;

    private Map<PlayerTag, TagEntry> tagCache = new HashMap<>();
    private Map<String, TagEntry> nullTagsCache = new HashMap<>();

    public TagPopup(Collection<PlayerTag> playerTags, Consumer<PlayerTag> callback) {
        super("Player tags");
        this.playerTags = playerTags;
        this.callback = callback;
    }

    public void show(Component invoker, int x, int y, String nullTag) {
        removeAll();
        for (PlayerTag tag : playerTags) {
            add(tagCache.computeIfAbsent(tag, TagEntry::new));
        }

        if (nullTag != null) {
            addSeparator();
            add(nullTagsCache.computeIfAbsent(nullTag, TagEntry::new));
        }
        show(invoker, x, y);
    }

    private class TagEntry extends JMenuItem implements ActionListener {
        private static final int ALPHA = 96;
        private PlayerTag tag = null;

        TagEntry(String name) {
            super(name);
            setOpaque(true);
            this.addActionListener(this);
        }

        TagEntry(PlayerTag tag) {
            this(tag.name);
            this.tag = tag;
            setBackground(UIUtils.blendColor(tag.color, ALPHA));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            callback.accept(tag);
        }

    }

}
