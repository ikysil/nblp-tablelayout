package de.berlios.nblayoutpack.tablelayout;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

import org.openide.ErrorManager;

import info.clearthought.layout.*;

/**
 * A customizer for TableLayout. It provides two tables for editing columns
 * and rows settings of TableLayout. Note it works with TableLayoutSupport,
 * not with TableLayout directly.
 *
 * @author Tomas Pavek
 */

public final class TableLayoutCustomizer extends javax.swing.JPanel {

    private TableLayoutSupport layoutSupport;

    private LayoutTableModel columnTableModel;
    private LayoutTableModel rowTableModel;

    private int[] selectedColumns;
    private int[] selectedRows;

    // --------
    // initialization

    /** Constructor - creates customizer instance for given TableLayoutSupport.
     */
    public TableLayoutCustomizer(TableLayoutSupport layoutSupport) {
        this.layoutSupport = layoutSupport;

        // setup GUI
        initComponents();

        // set bold font to column count and row count labels
        java.awt.Font font = columnLabel.getFont();
        if ((font.getStyle() & java.awt.Font.BOLD) == 0) {
            font = font.deriveFont(java.awt.Font.BOLD);
            columnLabel.setFont(font);
            rowLabel.setFont(font);
        }

        // initialize table models displaying columns and rows data
        columnTableModel = new LayoutTableModel();
        columnTable.setModel(columnTableModel);
        rowTableModel = new LayoutTableModel();
        rowTable.setModel(rowTableModel);

        // set width of the first columns (displaying numerical order)
        columnTable.getColumnModel().getColumn(0).setPreferredWidth(36);
        rowTable.getColumnModel().getColumn(0).setPreferredWidth(36);

        // setup cell renderers
        CellRenderer typeCellRenderer = new CellRenderer();
        columnTable.setDefaultRenderer(Double.class, typeCellRenderer);
        rowTable.setDefaultRenderer(Double.class, typeCellRenderer);

        CellRenderer numberCellRenderer = new CellRenderer();
        numberCellRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        columnTable.setDefaultRenderer(Integer.class, numberCellRenderer);
        rowTable.setDefaultRenderer(Integer.class, numberCellRenderer);

        // setup listener for columns
        Listener columnListener = new Listener(true);
        columnTable.addMouseListener(columnListener);
        columnTable.addKeyListener(columnListener);
        columnTable.getSelectionModel().addListSelectionListener(columnListener);
        columnParamCombo.getEditor().getEditorComponent()
            .addKeyListener(columnListener);
        addColumnsButton.addActionListener(columnListener);
        columnCountTextField1.addActionListener(columnListener);
        insertColumnsButton.addActionListener(columnListener);
        columnCountTextField2.addActionListener(columnListener);
        removeColumnsButton.addActionListener(columnListener);

        // setup listener for rows
        Listener rowListener = new Listener(false);
        rowTable.addMouseListener(rowListener);
        rowTable.addKeyListener(rowListener);
        rowTable.getSelectionModel().addListSelectionListener(rowListener);
        rowParamCombo.getEditor().getEditorComponent()
            .addKeyListener(rowListener);
        addRowsButton.addActionListener(rowListener);
        rowCountTextField1.addActionListener(rowListener);
        insertRowsButton.addActionListener(rowListener);
        rowCountTextField2.addActionListener(rowListener);
        removeRowsButton.addActionListener(rowListener);

        // fill in the tables
        updateColumns();
        updateRows();
    }

    /** Updates column table according to current data in the underlying
     * TableLayoutSupport.
     */
    void updateColumns() {
        // get new data from layout support
        double[] columns = layoutSupport.getColumns();

        // update info label
        columnLabel.setText(Integer.toString(columns.length) + "  Column"
                            + (columns.length == 1 ? "" : "s"));

        if (selectedColumns == null
            && columnTableModel.getData() != null
            && columns.length == columnTableModel.getRowCount())
        {   // remember current selection to restore it after new data is set
            selectedColumns = getSelectedIndices(
                                columnTable.getSelectionModel());
        }

        if ((selectedColumns == null || selectedColumns.length == 0)
            && columns.length > 0)
        {   // just the first row in the table will be selected
            selectedColumns = new int[] { 0 };
        }

        // add new data to table
        columnTableModel.setData(columns);

        if (columns.length == 0)
            columnParamCombo.getEditor().setItem("");
        else { // restore selection
            ListSelectionModel selection = columnTable.getSelectionModel();
            for (int i=0; i < selectedColumns.length; i++) {
                int sel = selectedColumns[i];
                selection.addSelectionInterval(sel, sel);
            }
            columnTable.getColumnModel().getSelectionModel()
                                          .setSelectionInterval(1, 1);
        }

        selectedColumns = null;
    }

    /** Updates row table according to current data in the underlying
     * TableLayoutSupport.
     */
    void updateRows() {
        // get new data layout support
        double[] rows = layoutSupport.getRows();

        // update info label
        rowLabel.setText(Integer.toString(rows.length) + "  Row"
                         + (rows.length == 1 ? "" : "s"));

        if (selectedRows == null
            && rowTableModel.getData() != null
            && rows.length == rowTableModel.getRowCount())
        {   // remember current selection to restore it after new data is set
            selectedRows = getSelectedIndices(rowTable.getSelectionModel());
        }

        if ((selectedRows == null || selectedRows.length == 0)
            && rows.length > 0)
        {   // just the first row in the table will be selected
            selectedRows = new int[] { 0 };
        }

        // add new data to table
        rowTableModel.setData(rows);

        if (rows.length == 0)
            rowParamCombo.getEditor().setItem("");
        else { // restore selection
            ListSelectionModel selection = rowTable.getSelectionModel();
            for (int i=0; i < selectedRows.length; i++) {
                int sel = selectedRows[i];
                selection.addSelectionInterval(sel, sel);
            }
            rowTable.getColumnModel().getSelectionModel()
                                       .setSelectionInterval(1, 1);
        }

        selectedRows = null;
    }

    // ----------
    // data setters

    /** Changes selected columns or rows in the underlying TableLayoutSupport
     * according to given value.
     */
    void changeData(boolean columns, double value, int[] selected) {
        double[] oldData =
            (columns ? columnTableModel : rowTableModel).getData();
        double[] newData = new double[oldData.length];

        int si = 0;
        for (int i=0; i < oldData.length; i++)
            if (si < selected.length && selected[si] == i) {
                newData[i] = value;
                si++;
            }
            else newData[i] = oldData[i];

        setData(columns, newData, selected);
    }

    /** Adds new columns or rows to the underlying TableLayoutSupport.
     */
    void addData(boolean columns, double value, int fromIndex, int count) {
        double[] oldData =
            (columns ? columnTableModel : rowTableModel).getData();
        double[] newData = new double[oldData.length + count];
        int[] select = new int[count];

        for (int i=0; i < fromIndex; i++)
            newData[i] = oldData[i];
        for (int i=0; i < count; i++) {
            newData[i + fromIndex] = value;
            select[i] = i + fromIndex;
        }
        for (int i=fromIndex; i < oldData.length; i++)
            newData[i + count] = oldData[i];

        setData(columns, newData, select);

        // adjust components positions
        for (int i=0, n=layoutSupport.getComponentCount(); i < n; i++) {
            TableLayoutSupport.TableLayoutSupportConstraints constr =
                (TableLayoutSupport.TableLayoutSupportConstraints)layoutSupport.getConstraints(i);
            try {
                if (columns) {
                    if(constr.right >= fromIndex){
                      constr.right += count;
                    }
                    if(constr.left >= fromIndex){
                      constr.left += count;
                    }
                }
                else {
                    if(constr.bottom >= fromIndex){
                      constr.bottom += count;
                    }
                    if(constr.top >= fromIndex){
                      constr.top += count;
                    }
                }
            }
            catch (Exception ex) {} // ignore, should not happen
        }
    }

    /** Removes selected columns or rows from the underlying TableLayoutSupport.
     */
    void removeData(boolean columns, int[] selected) {
        double[] oldData =
            (columns ? columnTableModel : rowTableModel).getData();
        double[] newData = new double[oldData.length - selected.length];

        int si = 0;
        for (int i=0; i < oldData.length; i++)
            if (si < selected.length && selected[si] == i)
                si++;
            else
                newData[i - si] = oldData[i];

        int[] select;
        if (newData.length > 0) {
            int selIndex = selected[0];
            if (selIndex >= newData.length)
                selIndex = newData.length - 1;
            select = new int[] { selIndex };
        }
        else select = null;

        setData(columns, newData, select);

        // adjust components positions
        for (int i=selected.length-1; i >= 0; i--) {
            int removedIndex = selected[i];
            for (int j=0, n=layoutSupport.getComponentCount(); j < n; j++) {
                TableLayoutSupport.TableLayoutSupportConstraints constr =
                    (TableLayoutSupport.TableLayoutSupportConstraints) layoutSupport.getConstraints(j);
                try {
                    if (columns) {
                        if (constr.left > removedIndex){
                          constr.left--;
                        }
                        if (constr.right > removedIndex){
                          constr.right--;
                        }
                    }
                    else {
                        if (constr.top > removedIndex){
                          constr.top--;
                        }
                        if (constr.bottom > removedIndex){
                          constr.bottom--;
                        }
                    }
                }
                catch (Exception ex) {} // ignore, should not happen
            }
        }
    }

    private void setData(boolean columns, double[] data, int[] select) {
      try{
        if (columns) {
            selectedColumns = select;
            layoutSupport.getProperty("columns").setValue(data);
        }
        else {
            selectedRows = select;
            layoutSupport.getProperty("rows").setValue(data);
        }
      }
      catch(Exception e){
        ErrorManager.getDefault().notify(e);
      }
    }

    // ----------
    // data getters (static methods)

    static String getStringFromValue(double value) {
        String str;
        if (value == TableLayout.PREFERRED)
            str = "PREFERRED";
        else if (value == TableLayout.FILL)
            str = "FILL";
        else if (value == TableLayout.MINIMUM)
            str = "MINIMUM";
        else
            str = Double.toString(value);
        return str;
    }

    static double getValueFromString(String str) {
        double value;
        if (str.equalsIgnoreCase("FILL"))
            value = TableLayout.FILL;
        else if (str.equalsIgnoreCase("PREF")
                 || str.equalsIgnoreCase("PREFERRED"))
            value = TableLayout.PREFERRED;
        else if (str.equalsIgnoreCase("MIN")
                 || str.equalsIgnoreCase("MINIMUM"))
            value = TableLayout.MINIMUM;
        else
            value = Double.parseDouble(str);
        return value;
    }

    static int[] getSelectedIndices(ListSelectionModel selection) {
        int i1 = selection.getMinSelectionIndex();
        int i2 = selection.getMaxSelectionIndex();
        if (i1 < 0 || i2 < i1)
            return new int[0]; // nothing selected

        int[] indices = new int[i2 - i1 + 1];
        int n = 0;
        for (int i=i1; i <= i2; i++)
            if (selection.isSelectedIndex(i))
                indices[n++] = i;

        if (n < indices.length) {
            int[] temp = new int[n];
            System.arraycopy(indices, 0, temp, 0, n);
            indices = temp;
        }

        return indices;
    }

    // ----------
    // inner classes

    /* A model for tables displaying columns and rows of the customized
     * TableLayout.
     */
    private static class LayoutTableModel extends AbstractTableModel{
        private double[] layoutData;

        void setData(double[] data){
            layoutData = data;
            fireTableDataChanged();
        }

        double[] getData() {
            return layoutData;
        }

        // --------

        public int getRowCount(){
          return layoutData == null ? 0 : layoutData.length;
        }

        public int getColumnCount(){
            return 2;
        }

        public Object getValueAt(int row, int column){
            switch (column) {
                case 0: return new Integer(row);
                case 1: return new Double(layoutData[row]);
            }
            throw new ArrayIndexOutOfBoundsException();
        }

        public Class getColumnClass(int column){
            switch (column) {
                case 0: return Integer.class;
                case 1: return Double.class;
            }
            throw new ArrayIndexOutOfBoundsException();
        }

        public String getColumnName(int column){
            switch (column) {
                case 0:
                case 1: return "";
            }
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    // --------

    /* A simple table cell renderer converting numbers to descriptive text.
     */
    private static class CellRenderer extends DefaultTableCellRenderer{
      protected void setValue(Object value){
        if (value instanceof Double) {
          double val = ((Double)value).doubleValue();
          if (val == TableLayout.PREFERRED)
            value = "PREFERRED";
          else if (val == TableLayout.FILL)
            value = "FILL";
          else if (val == TableLayout.MINIMUM)
            value = "MINIMUM";
          else if (val < 1.0)
            value = Integer.toString((int)(val * 100.0)) + " %";
          else
            value = Integer.toString((int)val) + " pts";
        }
        String str = getHorizontalAlignment() == RIGHT ? value.toString() + "  " :
                                                         "  " + value.toString();
        super.setValue(str);
      }
    }
    
    // ----------
    
    /* A listener class - listening to various events on components.
     */
    private class Listener extends MouseAdapter
    implements ListSelectionListener, KeyListener,
    ActionListener {
      private boolean onColumns; // watch columns (true) or rows (false)
      
      private Listener(boolean onColumns) {
        this.onColumns = onColumns;
      }
      
      // ------
      
      private JComboBox getParamCombo() {
        return onColumns ? columnParamCombo : rowParamCombo;
      }
      private JTable getTable() {
        return onColumns ? columnTable : rowTable;
      }
      private LayoutTableModel getTableModel() {
        return onColumns ? columnTableModel : rowTableModel;
      }
      private JButton getAddButton() {
        return onColumns ? addColumnsButton : addRowsButton;
      }
      private JTextField getAddTextField() {
        return onColumns ? columnCountTextField1 : rowCountTextField1;
      }
      private JButton getInsertButton() {
        return onColumns ? insertColumnsButton : insertRowsButton;
      }
      private JTextField getInsertTextField() {
        return onColumns ? columnCountTextField2 : rowCountTextField2;
      }
      private JButton getRemoveButton() {
        return onColumns ? removeColumnsButton : removeRowsButton;
      }
      
      // -----
      
      // table selection change
      public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
          ListSelectionModel selection = (ListSelectionModel) e.getSource();
          int startSelect = selection.getMinSelectionIndex();
          int endSelect = selection.getMaxSelectionIndex();
          
          if (startSelect >= 0) {
            Object value = getTableModel().getValueAt(startSelect, 1);
            double val = ((Double)value).doubleValue();
            getParamCombo().getEditor().setItem(getStringFromValue(val));
            
            getInsertButton().setEnabled(startSelect == endSelect);
            getInsertTextField().setEnabled(startSelect == endSelect);
            getRemoveButton().setEnabled(true);
          }
          else {
            getInsertButton().setEnabled(false);
            getInsertTextField().setEnabled(false);
            getRemoveButton().setEnabled(false);
          }
        }
      }
      
      // mouse click on table
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2
        && (e.getModifiers() & InputEvent.BUTTON1_MASK)
        == InputEvent.BUTTON1_MASK) {   // left mouse button doubleclick
          editValue();
          e.consume();
        }
      }
      
      // key press in table or in editing combobox
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          if (e.getSource() == getTable())
            editValue();
          else if (e.getSource() == getParamCombo().getEditor()
          .getEditorComponent())
            applyValue();
          
          e.consume();
        }
        else if (e.getSource() == getTable()) {
          if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            removeValues();
            e.consume();
          }
          else if (e.getKeyCode() == KeyEvent.VK_INSERT) {
            insertValue();
            e.consume();
          }
        }
      }
      public void keyTyped(KeyEvent e) {} // not interested in
      public void keyReleased(KeyEvent e) {} // not interested in
      
      // Add, Insert or Remove button press
      public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == getAddButton() || source == getAddTextField())
          addValue();
        else if (source == getInsertButton()
        || source == getInsertTextField())
          insertValue();
        else if (source == getRemoveButton())
          removeValues();
      }
      
      // -------
      
      private void editValue() {
        java.awt.Component editComp =
        getParamCombo().getEditor().getEditorComponent();
        
        editComp.requestFocus();
        if (editComp instanceof javax.swing.text.JTextComponent)
          ((javax.swing.text.JTextComponent)editComp).selectAll();
      }
      
      private void applyValue() {
        int[] selected = getSelectedIndices(getTable().getSelectionModel());
        if (selected.length > 0) {
          Object obj = getParamCombo().getEditor().getItem();
          double value;
          try {
            value = getValueFromString(obj.toString());
          }
          catch (NumberFormatException ex) { // silently ignore
            return;
          }
          
          changeData(onColumns, value, selected);
          getTable().requestFocus();
        }
        else if (getTableModel().getRowCount() == 0)
          addValue();
      }
      
      private void addValue() {
        Object obj = getParamCombo().getEditor().getItem();
        double value;
        try {
          value = getValueFromString(obj.toString());
        }
        catch (NumberFormatException ex) { // silently ignore
          return;
        }
        
        int count;
        try {
          count = Integer.parseInt(getAddTextField().getText());
        }
        catch (NumberFormatException ex) { // silently ignore
          return;
        }
        
        addData(onColumns, value, getTableModel().getRowCount(), count);
        getTable().requestFocus();
      }
      
      private void insertValue() {
        Object obj = getParamCombo().getEditor().getItem();
        double value;
        try {
          value = getValueFromString(obj.toString());
        }
        catch (NumberFormatException ex) { // silently ignore
          return;
        }
        
        int count;
        try {
          count = Integer.parseInt(getInsertTextField().getText());
        }
        catch (NumberFormatException ex) { // silently ignore
          return;
        }
        
        int index = getTable().getSelectionModel().getMinSelectionIndex();
        if (index >= 0) {
          addData(onColumns, value, index, count);
          getTable().requestFocus();
        }
      }
      
      private void removeValues() {
        int[] selected = getSelectedIndices(getTable().getSelectionModel());
        if (selected.length > 0) {
          removeData(onColumns, selected);
          getTable().requestFocus();
        }
      }
    }
    
    // ---------
    // GUI
    
    private void initComponents() {//GEN-BEGIN:initComponents
      info.clearthought.layout.TableLayout _tableLayoutInstance;
      info.clearthought.layout.TableLayout _tableLayoutInstance1;
      
      jPanel1 = new javax.swing.JPanel();
      columnLabel = new javax.swing.JLabel();
      jScrollPane1 = new javax.swing.JScrollPane();
      columnTable = new javax.swing.JTable();
      columnParamCombo = new javax.swing.JComboBox();
      addColumnsButton = new javax.swing.JButton();
      columnCountTextField1 = new javax.swing.JTextField();
      insertColumnsButton = new javax.swing.JButton();
      columnCountTextField2 = new javax.swing.JTextField();
      removeColumnsButton = new javax.swing.JButton();
      jPanel2 = new javax.swing.JPanel();
      rowLabel = new javax.swing.JLabel();
      jScrollPane2 = new javax.swing.JScrollPane();
      rowTable = new javax.swing.JTable();
      rowParamCombo = new javax.swing.JComboBox();
      addRowsButton = new javax.swing.JButton();
      rowCountTextField1 = new javax.swing.JTextField();
      insertRowsButton = new javax.swing.JButton();
      rowCountTextField2 = new javax.swing.JTextField();
      removeRowsButton = new javax.swing.JButton();
      
      setLayout(new java.awt.GridLayout(2, 1));
      
      _tableLayoutInstance1 = new info.clearthought.layout.TableLayout();
      _tableLayoutInstance1.setHGap(0);
      _tableLayoutInstance1.setVGap(0);
      _tableLayoutInstance1.setColumn(new double[]{6,info.clearthought.layout.TableLayout.FILL,8,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6});
      _tableLayoutInstance1.setRow(new double[]{6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,info.clearthought.layout.TableLayout.FILL,6});
      jPanel1.setLayout(_tableLayoutInstance1);
      
      jPanel1.setBorder(new javax.swing.border.MatteBorder(new java.awt.Insets(0, 0, 1, 0), java.awt.Color.gray));
      columnLabel.setDisplayedMnemonic('C');
      columnLabel.setLabelFor(columnTable);
      columnLabel.setText("X Columns");
      jPanel1.add(columnLabel, new info.clearthought.layout.TableLayoutConstraints(1, 1, 5, 1, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      columnTable.setToolTipText("Doubleclick or press Enter to edit the value");
      columnTable.setPreferredScrollableViewportSize(new java.awt.Dimension(100, 87));
      jScrollPane1.setViewportView(columnTable);
      
      jPanel1.add(jScrollPane1, new info.clearthought.layout.TableLayoutConstraints(1, 3, 1, 10, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      columnParamCombo.setEditable(true);
      columnParamCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "FILL", "MINIMUM", "PREFERRED" }));
      columnParamCombo.setToolTipText("Press Enter to apply the value to the selected columns");
      jPanel1.add(columnParamCombo, new info.clearthought.layout.TableLayoutConstraints(3, 3, 5, 3, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      addColumnsButton.setMnemonic('A');
      addColumnsButton.setText("Add");
      jPanel1.add(addColumnsButton, new info.clearthought.layout.TableLayoutConstraints(3, 5, 3, 5, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      columnCountTextField1.setColumns(3);
      columnCountTextField1.setText("1");
      jPanel1.add(columnCountTextField1, new info.clearthought.layout.TableLayoutConstraints(5, 5, 5, 5, info.clearthought.layout.TableLayout.CENTER, info.clearthought.layout.TableLayout.CENTER));
      
      insertColumnsButton.setMnemonic('I');
      insertColumnsButton.setText("Insert");
      jPanel1.add(insertColumnsButton, new info.clearthought.layout.TableLayoutConstraints(3, 7, 3, 7, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      columnCountTextField2.setColumns(3);
      columnCountTextField2.setText("1");
      jPanel1.add(columnCountTextField2, new info.clearthought.layout.TableLayoutConstraints(5, 7, 5, 7, info.clearthought.layout.TableLayout.CENTER, info.clearthought.layout.TableLayout.CENTER));
      
      removeColumnsButton.setMnemonic('e');
      removeColumnsButton.setText("Remove");
      jPanel1.add(removeColumnsButton, new info.clearthought.layout.TableLayoutConstraints(3, 9, 3, 9, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      add(jPanel1);
      
      _tableLayoutInstance = new info.clearthought.layout.TableLayout();
      _tableLayoutInstance.setHGap(0);
      _tableLayoutInstance.setVGap(0);
      _tableLayoutInstance.setColumn(new double[]{6,info.clearthought.layout.TableLayout.FILL,8,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6});
      _tableLayoutInstance.setRow(new double[]{6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,6,info.clearthought.layout.TableLayout.PREFERRED,info.clearthought.layout.TableLayout.FILL,6});
      jPanel2.setLayout(_tableLayoutInstance);
      
      jPanel2.setBorder(new javax.swing.border.MatteBorder(new java.awt.Insets(1, 0, 0, 0), java.awt.Color.white));
      rowLabel.setDisplayedMnemonic('R');
      rowLabel.setLabelFor(rowTable);
      rowLabel.setText("Y Rows");
      jPanel2.add(rowLabel, new info.clearthought.layout.TableLayoutConstraints(1, 1, 5, 1, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      rowTable.setToolTipText("Doubleclick or press Enter to edit the value");
      rowTable.setPreferredScrollableViewportSize(new java.awt.Dimension(100, 87));
      jScrollPane2.setViewportView(rowTable);
      
      jPanel2.add(jScrollPane2, new info.clearthought.layout.TableLayoutConstraints(1, 3, 1, 10, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      rowParamCombo.setEditable(true);
      rowParamCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "FILL", "MINIMUM", "PREFERRED" }));
      rowParamCombo.setToolTipText("Press Enter to apply the value to the selected rows");
      jPanel2.add(rowParamCombo, new info.clearthought.layout.TableLayoutConstraints(3, 3, 5, 3, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      addRowsButton.setMnemonic('d');
      addRowsButton.setText("Add");
      jPanel2.add(addRowsButton, new info.clearthought.layout.TableLayoutConstraints(3, 5, 3, 5, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      rowCountTextField1.setColumns(3);
      rowCountTextField1.setText("1");
      jPanel2.add(rowCountTextField1, new info.clearthought.layout.TableLayoutConstraints(5, 5, 5, 5, info.clearthought.layout.TableLayout.CENTER, info.clearthought.layout.TableLayout.CENTER));
      
      insertRowsButton.setMnemonic('n');
      insertRowsButton.setText("Insert");
      jPanel2.add(insertRowsButton, new info.clearthought.layout.TableLayoutConstraints(3, 7, 3, 7, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      rowCountTextField2.setColumns(3);
      rowCountTextField2.setText("1");
      jPanel2.add(rowCountTextField2, new info.clearthought.layout.TableLayoutConstraints(5, 7, 5, 7, info.clearthought.layout.TableLayout.CENTER, info.clearthought.layout.TableLayout.CENTER));
      
      removeRowsButton.setMnemonic('m');
      removeRowsButton.setText("Remove");
      jPanel2.add(removeRowsButton, new info.clearthought.layout.TableLayoutConstraints(3, 9, 3, 9, info.clearthought.layout.TableLayout.FULL, info.clearthought.layout.TableLayout.FULL));
      
      add(jPanel2);
      
    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addColumnsButton;
    private javax.swing.JButton addRowsButton;
    private javax.swing.JTextField columnCountTextField1;
    private javax.swing.JTextField columnCountTextField2;
    private javax.swing.JLabel columnLabel;
    private javax.swing.JComboBox columnParamCombo;
    private javax.swing.JTable columnTable;
    private javax.swing.JButton insertColumnsButton;
    private javax.swing.JButton insertRowsButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton removeColumnsButton;
    private javax.swing.JButton removeRowsButton;
    private javax.swing.JTextField rowCountTextField1;
    private javax.swing.JTextField rowCountTextField2;
    private javax.swing.JLabel rowLabel;
    private javax.swing.JComboBox rowParamCombo;
    private javax.swing.JTable rowTable;
    // End of variables declaration//GEN-END:variables
}
