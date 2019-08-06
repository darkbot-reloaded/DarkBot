package com.github.manolo8.darkbot.gui.utils;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.event.MouseEvent;

public class ToolTipHeader extends JTableHeader {
   private final GenericTableModel tableModel;

   public ToolTipHeader(TableColumnModel columnModel, GenericTableModel tableModel) {
      super(columnModel);
      this.tableModel = tableModel;
   }

   public String getToolTipText(MouseEvent e) {
      return tableModel.getToolTipAt(getTable().convertColumnIndexToModel(columnAtPoint(e.getPoint())));
   }

}