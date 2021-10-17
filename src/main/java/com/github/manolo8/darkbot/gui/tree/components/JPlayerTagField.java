package com.github.manolo8.darkbot.gui.tree.components;

import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.tree.ConfigField;
import com.github.manolo8.darkbot.config.types.Tag;
import com.github.manolo8.darkbot.config.types.TagDefault;
import com.github.manolo8.darkbot.gui.AdvancedConfig;
import com.github.manolo8.darkbot.gui.players.TagPopup;
import com.github.manolo8.darkbot.gui.tree.OptionEditor;
import com.github.manolo8.darkbot.gui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.stream.IntStream;

public class JPlayerTagField extends JButton implements OptionEditor {

    private static final int MARGIN = 16;
    private static final int ALPHA = 96;
    private ConfigField field;
    private TagPopup tagPopup;
    private TagDefault unset;

    public JPlayerTagField() {
        setEditing(null);

        addActionListener(e -> tagPopup.show(this, 0, getHeight(), unset.toString()));

        tagPopup = new TagPopup(ConfigEntity.INSTANCE.getConfig().PLAYER_TAGS, tag -> {
            if (field != null) {
                field.set(tag);
                setEditing(tag);
                JPlayerTagField.this.revalidate();
            }
        });
    }

    @Override
    public JComponent getComponent() {
        return this;
    }

    @Override
    public void edit(ConfigField field) {
        this.field = field;
        Tag tag = field.field.getAnnotation(Tag.class);
        if (tag == null) unset = TagDefault.UNSET;
        else unset = tag.value();

        setEditing(field.get());
    }

    private void setEditing(PlayerTag tag) {
        if (tag == null) {
            setText("(" + unset + ")");
            setBorder(null);
            setBackground(UIUtils.BACKGROUND);
        } else {
            setText(tag.name);
            setBorder(BorderFactory.createLineBorder(tag.color));
            setBackground(UIUtils.blendColor(tag.color, ALPHA));
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

        int maxTagSize = ConfigEntity.INSTANCE.getConfig().PLAYER_TAGS.stream()
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
