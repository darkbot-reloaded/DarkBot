package com.github.manolo8.darkbot.gui.players;

import com.formdev.flatlaf.FlatClientProperties;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.gui.utils.PopupButton;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import java.util.function.Consumer;

public class PlayerTagManager extends JPanel {

    private final PlayerEditor editor;

    private Consumer<PlayerTag> lastAction;
    private final TagPopup tags = new TagPopup(tag -> lastAction.accept(tag));

    public PlayerTagManager(PlayerEditor editor) {
        super(new MigLayout("ins 0, gap 0", "[][][]"));
        this.editor = editor;

        add(new TagButton(UIUtils.getIcon("add"), null, editor::addTagToPlayers, "Add new tag", true, false), "grow");
        add(new TagButton(UIUtils.getIcon("remove"), null, editor::removeTagFromPlayers, null, false, false), "grow");
        add(new TagButton(UIUtils.getIcon("close"), "Manage tags", editor::deleteTag, null, false, true));
    }

    void setup(Main main) {
        tags.setTags(main.config.PLAYER_TAGS);
    }

    private class TagButton extends PopupButton<TagPopup> {
        private final Consumer<PlayerTag> action;
        private final String nullTag;

        public TagButton(Icon icon, String text,
                         Consumer<PlayerTag> action,
                         String nullTag,
                         boolean leftBorder,
                         boolean rightBorder) {
            super(icon, text, tags);
            this.action = action;
            this.nullTag = nullTag;
            setBorder(UIUtils.getPartialBorder(1, leftBorder ? 1 : 0, 1, rightBorder ? 1 : 0));
            if (text == null)
                putClientProperty(FlatClientProperties.SQUARE_SIZE, true);
        }

        @Override
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            if (lastAction != this.action) return;
            super.popupMenuWillBecomeInvisible(e);
        }

        @Override
        protected void showPopup() {
            if (editor.main != null && editor.main.config.PLAYER_TAGS.isEmpty()) {
                editor.addTagToPlayers(null);
                return;
            }

            lastAction = this.action;
            popup.show(this, 0, getHeight() - 1, nullTag);
        }
    }

}
