package com.github.manolo8.darkbot.gui.tree.editors;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Tag;
import com.github.manolo8.darkbot.config.types.TagDefault;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.players.TagPopup;
import com.github.manolo8.darkbot.gui.utils.UIUtils;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.PlayerTag;
import eu.darkbot.api.config.util.OptionEditor;
import eu.darkbot.api.config.util.ValueHandler;
import eu.darkbot.api.managers.ConfigAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class PlayerTagEditor extends JButton implements OptionEditor<PlayerTag> {

    private static final int MARGIN = 16;
    private static final int ALPHA = 96;

    private final ConfigSetting<Config> root;

    private PlayerTag tag;
    private boolean shown = false;

    private TagPopup tagPopup;
    private TagDefault unset;

    public PlayerTagEditor(ConfigAPI api) {
        root = api.getConfigRoot();

        setEditing(null);
        addActionListener(e -> tagPopup.show(this, 0, getHeight(), unset.toString()));
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (shown) return; // Only show first time the editor is focused
                shown = true;      // For subsequent triggers, just click the button
                tagPopup.show(PlayerTagEditor.this, 0, getHeight(), unset.toString());
            }
        });

        tagPopup = new TagPopup(root.getValue().PLAYER_TAGS, this::setEditing);
    }

    @Override
    public JComponent getEditorComponent(ConfigSetting<PlayerTag> playerTag) {
        this.unset = playerTag.getMetadata("tagDefault");
        shown = false;
        tagPopup.setTags(root.getValue().PLAYER_TAGS);
        setEditing(playerTag.getValue());
        return this;
    }

    @Override
    public PlayerTag getEditorValue() {
        return tag;
    }

    private void setEditing(PlayerTag tag) {
        this.tag = tag;
        if (tag == null) {
            setText("(" + unset + ")");
            setBorder(null);
            setBackground(UIUtils.BACKGROUND);
        } else {
            setText(tag.getName());
            setBorder(BorderFactory.createLineBorder(tag.getColor()));
            setBackground(UIUtils.blendColor(tag.getColor(), ALPHA));
        }
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        setPreferredSize(new Dimension(getFontMetrics(getFont()).stringWidth(getText()) + MARGIN, 0));
    }

    @Override
    public Dimension getPreferredSize() {
        return AdvancedConfig.forcePreferredHeight(super.getPreferredSize());
    }

    @Override
    public Dimension getReservedSize() {
        FontMetrics fm = getFontMetrics(getFont());

        int maxTagSize = root.getValue().PLAYER_TAGS.stream()
                .map(t -> t.name)
                .mapToInt(fm::stringWidth)
                .max().orElse(50);

        return new Dimension(maxTagSize + MARGIN, 0);
    }

    @Override
    public boolean isDefaultButton() {
        return false;
    }
}
