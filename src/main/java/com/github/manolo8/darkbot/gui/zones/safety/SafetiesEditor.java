package com.github.manolo8.darkbot.gui.zones.safety;

import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.ConfigEntity;
import com.github.manolo8.darkbot.config.SafetyInfo;
import com.github.manolo8.darkbot.core.objects.Map;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Set;
import java.util.function.Consumer;

public class SafetiesEditor extends JPanel {

    Set<SafetyInfo> safetyInfos = null;
    SafetyInfo editing = null;

    private SafetiesList safetyList;
    private SafetiesDisplay safetyDisplay;
    private SafetyEditor safetyEditor;

    private Consumer<Map> onMapChange = m -> updateMap();

    public SafetiesEditor() {
        super(new MigLayout("ins 0, gap 0, wrap 2, fill", "[100px][grow]", "[grow][]"));
        initComponents();
        setComponentPosition();
    }

    public void setup(Main main) {
        main.config.ADDED_SAFETY.add(s -> {
            safetyList.addOrUpdate(s);
            safetyDisplay.repaint();
        });
        main.mapManager.mapChange.add(onMapChange);
        updateMap();
        safetyDisplay.setup(main);
    }

    private void initComponents() {
        safetyList = new SafetiesList(this);
        safetyDisplay = new SafetiesDisplay(this);
        safetyEditor = new SafetyEditor(this);
    }

    private void setComponentPosition() {
        JScrollPane safetyListWrapper = new JScrollPane(safetyList);
        safetyListWrapper.setBorder(BorderFactory.createEmptyBorder());
        add(safetyListWrapper, "grow");
        add(safetyDisplay, "grow");
        add(safetyEditor, "grow, span");
    }

    private void updateMap() {
        safetyInfos = ConfigEntity.INSTANCE.getOrCreateSafeties();
        safetyList.refresh();
        safetyDisplay.repaint();
    }

    protected void edit(SafetyInfo editing) {
        this.editing = null;
        safetyEditor.edit(editing);
        this.editing = editing;

        safetyList.setSelected(editing);
        safetyDisplay.repaint();
    }

}
