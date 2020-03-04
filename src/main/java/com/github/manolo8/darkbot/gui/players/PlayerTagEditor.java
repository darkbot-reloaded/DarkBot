package com.github.manolo8.darkbot.gui.players;

import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.gui.components.MainButton;
import com.github.manolo8.darkbot.gui.utils.PopupMenuListenerAdapter;
import com.github.manolo8.darkbot.gui.utils.SimpleMouseListener;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class PlayerTagEditor extends JPanel {
    private PlayerEditor editor;

    private TagButton latestClicked;
    private TagPopup tags;

    public PlayerTagEditor(PlayerEditor editor) {
        super(new MigLayout("ins 0, gap 0", "[][][]"));
        this.editor = editor;

        add(new TagButton(UIUtils.getIcon("add"), null, editor::addTagToPlayers, "Add new tag", true, false));
        add(new TagButton(UIUtils.getIcon("remove"), null, editor::removeTagFromPlayers, null, false, false));
        add(new TagButton(UIUtils.getIcon("close"), "Manage tags", editor::deleteTag, null, false, true));
        tags = new TagPopup(editor.main.config.PLAYER_TAGS, tag -> latestClicked.action.accept(tag));
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

        tags.show(clicked, 0, getHeight() - 1, clicked.nullTag);
    }

    private class TagButton extends MainButton implements SimpleMouseListener {
        private long keepClosed;
        private Consumer<PlayerTag> action;
        private String nullTag;

        public TagButton(Icon icon, String text,
                         Consumer<PlayerTag> action,
                         String nullTag,
                         boolean leftBorder,
                         boolean rightBorder) {
            super(icon, text);
            this.action = action;
            this.nullTag = nullTag;
            setBorder(UIUtils.getPartialBorder(1, leftBorder ? 1 : 0, 1, rightBorder ? 1 : 0));
            addMouseListener(this);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (keepClosed > System.currentTimeMillis()) keepClosed = Long.MAX_VALUE;
        }

        @Override
        public void mouseExited(MouseEvent e) {
            keepClosed = 0;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            showPopup(this);
        }
    }

}
