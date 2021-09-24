package com.github.manolo8.darkbot.gui.utils;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.MouseEvent;

public class ToolTipHeader extends JTableHeader {
   private GenericTableModel<?> tableModel;

   public ToolTipHeader(TableColumnModel columnModel) {
      this(columnModel, null);
   }

   public ToolTipHeader(TableColumnModel columnModel, GenericTableModel<?> tableModel) {
      super(columnModel);
      this.tableModel = tableModel;
   }

   public String getToolTipText(MouseEvent e) {
      if (tableModel == null) return null;
      return tableModel.getToolTipAt(getTable().convertColumnIndexToModel(columnAtPoint(e.getPoint())));
   }

   public void setTableHeader(GenericTableModel<?> tableModel) {
      this.tableModel = tableModel;
   }

}