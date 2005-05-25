/*
 * TableLayoutSupport.java
 *
 * Created on December 19, 2004, 7:17 PM
 */

package de.berlios.nblayoutpack.tablelayout;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.*;
import java.io.IOException;
import java.text.*;
import java.util.*;

import javax.swing.*;

import org.openide.ErrorManager;
import org.openide.explorer.propertysheet.editors.*;
import org.openide.nodes.*;
import org.openide.util.Utilities;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.*;

import info.clearthought.layout.*;

import de.berlios.nblayoutpack.tablelayout.converter.*;

/**
 *
 * @author  Illya Kysil
 */
public class TableLayoutSupport extends AbstractLayoutSupport{
  
  /** The icon for TableLayout. */
  private static String iconURL =
      "de/berlios/nblayoutpack/tablelayout/resources/tablelayout16.png"; // NOI18N
  /** The icon for TableLayout. */
  private static String icon32URL =
      "de/berlios/nblayoutpack/tablelayout/resources/tablelayout32.png"; // NOI18N

  private TableLayoutCustomizer layoutCustomizer;
  private JDialog customizerDialog;

  /** Creates a new instance of TableLayoutSupport */
  public TableLayoutSupport(){
  }
  
  public Class getSupportedClass(){
    return TableLayout.class;
  }
  
  /** Provides an icon to be used for the layout node in Component
   * Inspector. Only 16x16 color icon is required.
   * @param type is one of BeanInfo constants: ICON_COLOR_16x16,
   *        ICON_COLOR_32x32, ICON_MONO_16x16, ICON_MONO_32x32
   * @return icon to be displayed for node in Component Inspector
   */
  public Image getIcon(int type){
    switch (type) {
      case BeanInfo.ICON_COLOR_16x16:
      case BeanInfo.ICON_MONO_16x16:
        return Utilities.loadImage(iconURL);
      default:
        return Utilities.loadImage(icon32URL);
    }
  }
  
  /** Returns a class of a customizer for the layout manager being used as
   * a JavaBean. The class should be a java.awt.Component and
   * java.beans.Customizer.
   * @return layout bean customizer class, null if no customizer is provided
   */
  public Class getCustomizerClass() {
    return TableLayoutCustomizer.class;
  }
  
  /** Returns an instance of a special customizer provided by the layout
   * support delegate. Since its creation is under full control, it can use
   * any interface, not necessarily the cumbersome java.beans.Customizer.
   * It can operate on metadata level (unlike the bean customizer which
   * works only with layout manager bean instance).
   * @return instance of layout support customizer
   */
  public Component getSupportCustomizer(){
    if(layoutCustomizer == null || customizerDialog == null){
      layoutCustomizer = new TableLayoutCustomizer(this);
      customizerDialog = createCustomizerDialog();
    }
    return customizerDialog;
  }
  
  /** This method is called after a property of the layout is changed by
   * the user. Subclasses may check whether the layout is valid after the
   * change and throw PropertyVetoException if the change should be reverted.
   * We use this method just to notify the customizer about the change.
   * @param ev PropertyChangeEvent object describing the change
   */
  public void acceptContainerLayoutChange(java.beans.PropertyChangeEvent ev)
  throws java.beans.PropertyVetoException{
    super.acceptContainerLayoutChange(ev);
    if(layoutCustomizer != null){
      layoutCustomizer.updateColumns();
      layoutCustomizer.updateRows();
    }
  }
  
  public LayoutConstraints createDefaultConstraints(){
    return new TableLayoutSupportConstraints();
  }
  
  /** Called from createComponentCode method, creates code for a component
   * layout constraints (opposite to readConstraintsCode).
   * @param constrCode CodeGroup to be filled with constraints code; not
   *        needed here because AbsoluteConstraints object is represented
   *        only by a single constructor code expression and no statements
   * @param constr layout constraints metaobject representing the constraints
   * @param compExp CodeExpression object representing the component; not
   *        needed here
   * @return created CodeExpression representing the layout constraints
   */
  protected CodeExpression createConstraintsCode(CodeGroup constrCode,
  LayoutConstraints constr, CodeExpression compExp, int index){
    if(!(constr instanceof TableLayoutSupportConstraints))
      return null;
    
    TableLayoutSupportConstraints tlsConstr = (TableLayoutSupportConstraints)constr;
    // code expressions for constructor parameters are created in
    // TableLayoutSupportConstraints
    CodeExpression[] params = tlsConstr.createPropertyExpressions(getCodeStructure());
    return getCodeStructure().createExpression(TableLayoutIntrospector.getConstraintsConstructor(), params);
  }
  
  protected LayoutConstraints readConstraintsCode(CodeExpression constrExp, CodeGroup constrCode, CodeExpression compExp){
    TableLayoutSupportConstraints constr = new TableLayoutSupportConstraints();
    // reading is done in TableLayoutSupportConstraints
    constr.readCodeExpression(constrExp, constrCode);
    return constr;
  }
  
  protected static ResourceBundle getBundle(){
    return org.openide.util.NbBundle.getBundle(TableLayoutSupport.class);
  }

  private FormProperty[] properties;
  
  protected FormProperty[] getProperties(){
    if(properties == null) {
      properties = createProperties();
    }
    return properties;
  }

  public Node.Property getProperty(String propName){
    FormProperty[] props = getProperties();
    for(int i = 0; i < props.length; i++){
      if(props[i].getName().equals(propName)){
        return props[i];
      }
    }
    return null;
  }
  
  private int hGap;
  private int vGap;
  private double[] columns = new double[0];
  private double[] rows = new double[0];
  
  protected FormProperty[] createProperties(){
    FormProperty[] properties = new FormProperty[]{
      new FormProperty("hGap", // NOI18N
                       Integer.TYPE,
                       getBundle().getString("PROP_hGap"), // NOI18N
                       getBundle().getString("HINT_hGap")){ // NOI18N

        public Object getTargetValue(){
          return new Integer(hGap);
        }
        public void setTargetValue(Object value){
          hGap = ((Integer)value).intValue();
        }
        public void setPropertyContext(FormPropertyContext ctx){
          // disabling this method due to limited persistence
          // capabilities (compatibility with previous versions)
        } 
      },

      new FormProperty("vGap", // NOI18N
                       Integer.TYPE,
                       getBundle().getString("PROP_vGap"), // NOI18N
                       getBundle().getString("HINT_vGap")){ // NOI18N

        public Object getTargetValue(){
          return new Integer(vGap);
        }
        public void setTargetValue(Object value){
          vGap = ((Integer)value).intValue();
        }
        public void setPropertyContext(FormPropertyContext ctx){
          // disabling this method due to limited persistence
          // capabilities (compatibility with previous versions)
        } 
      },

      new FormProperty("columns", // NOI18N
                       double[].class,
                       getBundle().getString("PROP_columns"), // NOI18N
                       getBundle().getString("HINT_columns")){ // NOI18N

        public Object getTargetValue(){
          return columns;
        }
        public void setTargetValue(Object value){
          columns = (double[])value;
        }
        public void setPropertyContext(FormPropertyContext ctx){
          // disabling this method due to limited persistence
          // capabilities (compatibility with previous versions)
        } 
        public PropertyEditor getExpliciteEditor(){
          return new ColumnsEditor();
        }
      },

      new FormProperty("rows", // NOI18N
                       double[].class,
                       getBundle().getString("PROP_rows"), // NOI18N
                       getBundle().getString("HINT_rows")){ // NOI18N

        public Object getTargetValue(){
          return rows;
        }
        public void setTargetValue(Object value){
          rows = (double[])value;
        }
        public void setPropertyContext(FormPropertyContext ctx){
          // disabling this method due to limited persistence
          // capabilities (compatibility with previous versions)
        } 
        public PropertyEditor getExpliciteEditor(){
          return new RowsEditor();
        }
      },
    };
    properties[2].setValue("canEditAsText", Boolean.TRUE); // NOI18N
    properties[3].setValue("canEditAsText", Boolean.TRUE); // NOI18N
    return properties;
  }

  private static final String baseVarName = "_tableLayoutInstance";

  /** Creates code structures for a new layout manager (opposite to
   * readInitLayoutCode). As the TableLayout is not a bean, this method must
   * override from AbstractLayoutSupport.
   * @param layoutCode CodeGroup to be filled with relevant
   *        initialization code;
   * @return new CodeExpression representing the TableLayout
   */
  protected CodeExpression createInitLayoutCode(CodeGroup layoutCode){
    CodeStructure codeStructure = getCodeStructure();
    CodeExpression varExpression = codeStructure.createExpression(TableLayoutIntrospector.getLayoutConstructor(), new CodeExpression[0]);
    String varName = baseVarName;
    CodeVariable var = codeStructure.getVariable(varName);
    int i = 1;
    while(var != null){
      varName = baseVarName + (i++);
      var = codeStructure.getVariable(varName);
    };
    var = codeStructure.createVariable(CodeVariable.LOCAL | CodeVariable.EXPLICIT_DECLARATION, TableLayout.class, varName);
    codeStructure.attachExpressionToVariable(varExpression, var);
    layoutCode.addStatement(0, var.getAssignment(varExpression));
    FormProperty[] properties = getProperties();
    CodeExpression[] setHGapParams = new CodeExpression[1];
    setHGapParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[0]));
    layoutCode.addStatement(codeStructure.createStatement(varExpression, TableLayoutIntrospector.getSetHGapMethod(), setHGapParams));
    CodeExpression[] setVGapParams = new CodeExpression[1];
    setVGapParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[1]));
    layoutCode.addStatement(codeStructure.createStatement(varExpression, TableLayoutIntrospector.getSetVGapMethod(), setVGapParams));
    CodeExpression[] setColumnParams = new CodeExpression[1];
    setColumnParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[2]));
    layoutCode.addStatement(codeStructure.createStatement(varExpression, TableLayoutIntrospector.getSetColumnMethod(), setColumnParams));
    CodeExpression[] setRowParams = new CodeExpression[1];
    setRowParams[0] = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[3]));
    layoutCode.addStatement(codeStructure.createStatement(varExpression, TableLayoutIntrospector.getSetRowMethod(), setRowParams));
    return varExpression;
  }

  protected void readInitLayoutCode(CodeExpression codeExpression, CodeGroup layoutCode){
    CodeVariable var = codeExpression.getVariable();
    layoutCode.addStatement(0, var.getAssignment(codeExpression));
    Iterator it = CodeStructure.getDefinedStatementsIterator(codeExpression);
    FormProperty[] properties = getProperties();
    while(it.hasNext()){
      CodeStatement statement = (CodeStatement)it.next();
      if(isMethod(statement, TableLayoutIntrospector.getSetHGapMethod())){
        FormCodeSupport.readPropertyStatement(statement, properties[0], true);
      }
      if(isMethod(statement, TableLayoutIntrospector.getSetVGapMethod())){
        FormCodeSupport.readPropertyStatement(statement, properties[1], true);
      }
      if(isMethod(statement, TableLayoutIntrospector.getSetColumnMethod())){
        FormCodeSupport.readPropertyStatement(statement, properties[2], true);
      }
      if(isMethod(statement, TableLayoutIntrospector.getSetRowMethod())){
        FormCodeSupport.readPropertyStatement(statement, properties[3], true);
      }
      layoutCode.addStatement(statement);
    }
    updateLayoutInstance();
  }

  public boolean isMethod(CodeStatement statement, Method method){
    Object obj = statement.getMetaObject();
    if (obj != null && obj instanceof Method) {
      Method other = (Method)obj;
      // Compare class names only since classes can be loaded by different ClassLoaders
      if((method.getDeclaringClass().getName().equals(other.getDeclaringClass().getName()))
         && (method.getName() == other.getName())) {
        if(!method.getReturnType().equals(other.getReturnType())){
          return false;
        }
        Class[] params1 = method.getParameterTypes();
        Class[] params2 = other.getParameterTypes();
        if (params1.length == params2.length) {
          for (int i = 0; i < params1.length; i++) {
            if (params1[i] != params2[i])
              return false;
          }
          return true;
        }
      }
    }
    return false;
  }

  /** Sets up the layout (without adding components) on a real container,
   * according to the internal metadata representation. This method must
   * override AbstractLayoutSupport because TableLayout instance cannot
   * be used universally - new instance must be created for each container.
   * @param container instance of a real container to be set
   * @param containerDelegate effective container delegate of the container;
   *        for layout managers we always use container delegate instead of
   *        the container
   */
  public void setLayoutToContainer(Container container, Container containerDelegate){
    try{
      containerDelegate.setLayout(cloneLayoutInstance(container, containerDelegate));
    }
    catch(Exception e){}
  }

  protected LayoutManager cloneLayoutInstance(Container container, Container containerDelegate){
    TableLayout result = new TableLayout();
    result.setHGap(hGap);
    result.setVGap(vGap);
    result.setColumn(columns);
    result.setRow(rows);
    return result;
  }
  
  /** This method should calculate layout constraints for a component dragged
   * over a container (or just for mouse cursor being moved over container).
   * @param container instance of a real container over/in which the
   *        component is dragged
   * @param containerDelegate effective container delegate of the container
   *        (e.g. like content pane of JFrame)
   * @param component the real component being dragged, can be null
   * @param index position (index) of the component in its current container;
   *        -1 if there's no dragged component
   * @param posInCont position of mouse in the container delegate
   * @param posInComp position of mouse in the dragged component; null if
   *        there's no dragged component
   * @return new LayoutConstraints object corresponding to the position of
   *         the component in the container; may return null if the layout
   *         does not use component constraints, or if default constraints
   *         should be used
   */
  public LayoutConstraints getNewConstraints(Container container, Container containerDelegate,
  Component component, int index, Point posInCont, Point posInComp){
    TableLayout tableLayout = (TableLayout) containerDelegate.getLayout();
    TableLayoutSupportConstraints tlConstr = (TableLayoutSupportConstraints)getConstraints(index);
    TableLayoutIntrospector.calculateSize(tableLayout, containerDelegate);
    int[][] crSize = TableLayoutIntrospector.getCrSize(tableLayout);
    int[][] crOffset = TableLayoutIntrospector.getCrOffset(tableLayout);
    if((crSize == null) || (crOffset == null)){
      return new TableLayoutSupportConstraints();
    }
    int[] columnSize = crSize[0];
    int[] rowSize = crSize[1];
    int[] columnOffset = crOffset[0];
    int[] rowOffset = crOffset[1];
    int x = posInCont.x;
    int y = posInCont.y;
    int column = 0;
    int row = 0;
    
    int hGap = tableLayout.getHGap();
    int vGap = tableLayout.getVGap();
    int gap = 0;
    for(int i = 0; i < columnSize.length; i++){
      if(x >= columnOffset[i] + gap){
        column = i;
      }
      if(x < columnOffset[i + 1] + gap){
        break;
      }
      gap += hGap;
    }
    
    gap = 0;
    for(int i = 0; i < rowSize.length; i++){
      if(y >= rowOffset[i] + gap){
        row = i;
      }
      if(y < rowOffset[i + 1] + gap){
        break;
      }
      gap += vGap;
    }
    
    int hAlign = tlConstr != null ? tlConstr.hAlign : TableLayout.FULL;
    int vAlign = tlConstr != null ? tlConstr.vAlign : TableLayout.FULL;
    return new TableLayoutSupportConstraints(column, row, column, row, hAlign, vAlign);
  }
  
  /** This method should paint a feedback for a component dragged over
   * a container (or just for mouse cursor being moved over container).
   * In principle, it should present given component layout constraints or
   * index graphically.
   * @param container instance of a real container over/in which the
   *        component is dragged
   * @param containerDelegate effective container delegate of the container
   *        (e.g. like content pane of JFrame) - here the feedback is painted
   * @param component the real component being dragged, can be null
   * @param newConstraints component layout constraints to be presented
   * @param newIndex component's index position to be presented
   *        (if newConstraints == null)
   * @param g Graphics object for painting (with color and line style set)
   * @return whether any feedback was painted (may return false if the
   *         constraints or index are invalid, or if the painting is not
   *         implemented)
   */
  public boolean paintDragFeedback(Container container, Container containerDelegate,
  Component component, LayoutConstraints newConstraints, int newIndex, Graphics g){
    TableLayout tableLayout = (TableLayout)containerDelegate.getLayout();
    TableLayoutSupportConstraints tlConstr = (TableLayoutSupportConstraints)newConstraints;
    TableLayoutIntrospector.calculateSize(tableLayout, containerDelegate);
    int[][] crSize = TableLayoutIntrospector.getCrSize(tableLayout);
    if(crSize == null){
      return false;
    }
    int[] columnSize = crSize[0];
    int[] rowSize = crSize[1];

    int counter;

    Color defColor = g.getColor();
    Rectangle sRect = new Rectangle(-1, -1, 0, 0);
    int y = 0;
    for(int row = 0; row < rowSize.length; row++){
      if((row >= tlConstr.top) && (row <= tlConstr.bottom)){
        if(sRect.y < 0){
          sRect.y = y;
        }
        sRect.height += rowSize[row] + tableLayout.getVGap();
      }
      g.drawLine(0, y, containerDelegate.getWidth(), y);
      y += rowSize[row] + tableLayout.getVGap();
    }
    g.drawLine(0, y, containerDelegate.getWidth(), y);
    int x = 0;
    for(int column = 0; column < columnSize.length; column++){
      if((column >= tlConstr.left) && (column <= tlConstr.right)){
        if(sRect.x < 0){
          sRect.x = x;
        }
        sRect.width += columnSize[column] + tableLayout.getHGap();
      }
      g.drawLine(x, 0, x, containerDelegate.getHeight());
      x += columnSize[column] + tableLayout.getHGap();
    }
    g.drawLine(x, 0, x, containerDelegate.getHeight());
    g.setColor(Color.red);
    g.drawRect(sRect.x, sRect.y, sRect.width, sRect.height);
    return true;
  }
  
  /** Provides resizing options for given component. It can combine the
   * bit-flag constants RESIZE_UP, RESIZE_DOWN, RESIZE_LEFT, RESIZE_RIGHT.
   * @param container instance of a real container in which the
   *        component is to be resized
   * @param containerDelegate effective container delegate of the container
   *        (e.g. like content pane of JFrame)
   * @param component real component to be resized
   * @param index position of the component in its container
   * @return resizing options for the component; 0 if no resizing is possible
   */
  public int getResizableDirections(Container container, Container containerDelegate,
  Component component, int index){
    TableLayout tableLayout = (TableLayout)containerDelegate.getLayout();
    TableLayoutSupportConstraints tlConstr = (TableLayoutSupportConstraints)getConstraints(index);
    
    int resizable = 0;
    
    if((tlConstr.left > 0) || (tlConstr.left < tlConstr.right)){
      resizable |= RESIZE_LEFT;
    }
    
    if((tlConstr.right + 1 < tableLayout.getNumColumn()) ||
       (tlConstr.right > tlConstr.left)){
      resizable |= RESIZE_RIGHT;
    }
    
    if((tlConstr.top > 0) || (tlConstr.top < tlConstr.bottom)){
      resizable |= RESIZE_UP;
    }
    
    if((tlConstr.bottom + 1 < tableLayout.getNumRow()) ||
       (tlConstr.bottom > tlConstr.top)){
      resizable |= RESIZE_DOWN;
    }
    
    return resizable;
  }
  
  /** This method should calculate layout constraints for a component being
   * resized.
   * @param container instance of a real container in which the
   *        component is to be resized
   * @param containerDelegate effective container delegate of the container
   *        (e.g. like content pane of JFrame)
   * @param component real component to be resized
   * @param index position of the component in its container
   * @param sizeChanges Insets object with size differences
   * @param posInCont position of mouse in the container delegate
   * @return component layout constraints for resized component; null if
   *         resizing is not possible or not implemented
   */
  public LayoutConstraints getResizedConstraints(Container container, Container containerDelegate,
  Component component, int index, Insets sizeChanges, Point posInCont){
    TableLayout tableLayout = (TableLayout)containerDelegate.getLayout();
    TableLayoutIntrospector.calculateSize(tableLayout, containerDelegate);
    int[][] crSize = TableLayoutIntrospector.getCrSize(tableLayout);
    int[][] crOffset = TableLayoutIntrospector.getCrOffset(tableLayout);
    if((crSize == null) || (crOffset == null)){
      return new TableLayoutSupportConstraints();
    }
    int[] columnSize = crSize[0];
    int[] rowSize = crSize[1];
    int[] columnOffset = crOffset[0];
    int[] rowOffset = crOffset[1];
    TableLayoutSupportConstraints tlConstr = (TableLayoutSupportConstraints) getConstraints(index);
    
    int left;
    if(sizeChanges.left != 0){
      left = 0;
      for(int i = 0; i < columnSize.length; i++){
        if(posInCont.x >= columnOffset[i]){
          left = i;
        }
        if(posInCont.x < columnOffset[i + 1]){
          break;
        }
      }
      if(left > tlConstr.right)
        left = tlConstr.right;
    }
    else{
      left = tlConstr.left;
    }
    
    int top;
    if(sizeChanges.top != 0){
      top = 0;
      for(int i = 0; i < rowSize.length; i++){
        if(posInCont.y >= rowOffset[i]){
          top = i;
        }
        if(posInCont.y < rowOffset[i + 1]){
          break;
        }
      }
      if(top > tlConstr.bottom){
        top = tlConstr.bottom;
      }
    }
    else{
      top = tlConstr.top;
    }
    
    int right;
    if(sizeChanges.right != 0) {
      right = tableLayout.getNumColumn() - 1;
      for(int i = columnSize.length; i > 0; i--){
        if(posInCont.x < columnOffset[i]){
          right = i - 1;
        }
        if(posInCont.x >= columnOffset[i - 1]){
          break;
        }
      }
      if(right < tlConstr.left){
        right = tlConstr.left;
      }
    }
    else{
      right = tlConstr.right;
    }
    
    int bottom;
    if(sizeChanges.bottom != 0){
      bottom = tableLayout.getNumRow() - 1;
      for(int i = rowSize.length; i > 0; i--){
        if(posInCont.y < rowOffset[i]){
          bottom = i - 1;
        }
        if(posInCont.y >= rowOffset[i - 1]){
          break;
        }
      }
      if(bottom < tlConstr.top){
        bottom = tlConstr.top;
      }
    }
    else{
      bottom = tlConstr.bottom;
    }
    return new TableLayoutSupportConstraints(left, top, right, bottom, tlConstr.hAlign, tlConstr.vAlign);
  }

  /** This method is called when switching layout - giving an opportunity to
   * convert the previous constrainst of components to constraints of the new
   * layout (this layout). The default implementation does nothing.
   * @param previousConstraints [input] layout constraints of components in
   *                                    the previous layout
   * @param currentConstraints [output] array of converted constraints for
   *                                    the new layout - to be filled
   * @param components [input] real components in a real container having the
   *                           previous layout
   */
  public void convertConstraints(LayoutConstraints[] previousConstraints,
  LayoutConstraints[] currentConstraints, Component[] components){
    if((components == null) || (components.length == 0)){
      return;
    }
    ConstraintsConverter converter = new DefaultConstraintsConverter();
    converter.convertConstraints(getLayoutContext(), this, previousConstraints, currentConstraints, components);
  }

  /**
   * Getter for property columns.
   * @return Value of property columns.
   */
  public double[] getColumns(){
    return this.columns;
  }
  
  /**
   * Setter for property columns.
   * @param columns New value of property columns.
   */
  public void setColumns(double[] columns){
    this.columns = columns;
  }
  
  /**
   * Getter for property rows.
   * @return Value of property rows.
   */
  public double[] getRows(){
    return this.rows;
  }
  
  /**
   * Setter for property rows.
   * @param rows New value of property rows.
   */
  public void setRows(double[] rows) {
    this.rows = rows;
  }
  
  private String getContainerName(){
    CodeVariable var = getLayoutContext().getContainerCodeExpression().getVariable();
    return var != null ? var.getName() : null;
  }
  
  // ---------
  // CustomizerDialog - a non-modal dialog holding the customizer itself.
  // Some tweaks are required to obtain a non-modal dialog behaving like
  // a floating window reasonably.
  
  private JDialog createCustomizerDialog() {
    Frame dialogOwner = null;
    Point designLocation = null;
    
    Component activeComp =
    org.openide.windows.TopComponent.getRegistry().getActivated();
    if (activeComp != null) {
      Component comp = activeComp.getParent();
      while (comp != null) {
        if (comp instanceof Frame) {
          dialogOwner = (Frame) comp;
          break;
        }
        comp = comp.getParent();
      }
    }
    
    if (dialogOwner != null) {
      designLocation = activeComp.getLocation();
      SwingUtilities.convertPointToScreen(
      designLocation, activeComp.getParent());
    }
    else dialogOwner = org.openide.windows.WindowManager.getDefault()
    .getMainWindow();
    
    String title = getContainerName();
    title = (title != null ? title : "Form") + " -> " + "TableLayout";
    
    JDialog dialog = new CustomizerDialog(dialogOwner, title, false);
    dialog.getContentPane().add(layoutCustomizer);
    dialog.pack();
    
    if (designLocation != null) {
      designLocation.x -= dialog.getWidth() + 1;
      if (designLocation.x < 0)
        designLocation.x = 0;
      if (designLocation.y < 0)
        designLocation.y = 0;
      dialog.setLocation(designLocation);
    }
    
    return dialog;
  }
  
  private class CustomizerDialog extends JDialog {
    CustomizerDialog(Frame owner, String title, boolean modal) {
      super(owner, title, modal);
      setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }
    
    public void setVisible(boolean value) {
      super.setVisible(value);
      if (value == false && layoutCustomizer != null) {
        layoutCustomizer = null;
        customizerDialog = null;
      }
    }
  }

  public class TableLayoutSupportConstraints implements LayoutConstraints{

    private Node.Property[] properties;

    public int left;
    public int top;
    public int right;
    public int bottom;
    public int hAlign;
    public int vAlign;

    /** Creates a new instance of TableLayoutConstraints */
    public TableLayoutSupportConstraints(){
      TableLayoutConstraints tlc = new TableLayoutConstraints();
      left = tlc.col1;
      top = tlc.row1;
      right = tlc.col2;
      bottom = tlc.row2;
      hAlign = tlc.hAlign;
      vAlign = tlc.vAlign;
    }

    public TableLayoutSupportConstraints(TableLayoutSupportConstraints prototype){
      this.left = prototype.left;
      this.top = prototype.top;
      this.right = prototype.right;
      this.bottom = prototype.bottom;
      this.hAlign = prototype.hAlign;
      this.vAlign = prototype.vAlign;
    }

    public TableLayoutSupportConstraints(int left, int top, int right, int bottom,
    int hAlign, int vAlign){
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
      this.hAlign = hAlign;
      this.vAlign = vAlign;
    }

    public Node.Property[] getProperties(){
      if(properties == null) {
        properties = createProperties();
        reinstateProperties();
      }
      return properties;
    }

    public Object getConstraintsObject(){
      return new TableLayoutConstraints(left, top, right, bottom, hAlign, vAlign);
    }

    public LayoutConstraints cloneConstraints(){
      return new TableLayoutSupportConstraints(this);
    }

    protected Node.Property[] createProperties(){
      return new Node.Property[]{
        new FormProperty("TableLayoutConstraints.left", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_left"), // NOI18N
                         getBundle().getString("HINT_left")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(left);
          }
          public void setTargetValue(Object value){
            left = ((Integer)value).intValue();
            if(right < left){
              right = left;
            }
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
        },

        new FormProperty("TableLayoutConstraints.top", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_top"), // NOI18N
                         getBundle().getString("HINT_top")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(top);
          }
          public void setTargetValue(Object value){
            top = ((Integer)value).intValue();
            if(bottom < top){
              bottom = top;
            }
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
        },

        new FormProperty("TableLayoutConstraints.right", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_right"), // NOI18N
                         getBundle().getString("HINT_right")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(right);
          }
          public void setTargetValue(Object value){
            right = ((Integer)value).intValue();
            if(right < left){
              left = right;
            }
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          }
        },

        new FormProperty("TableLayoutConstraints.bottom", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_bottom"), // NOI18N
                         getBundle().getString("HINT_bottom")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(bottom);
          }
          public void setTargetValue(Object value){
            bottom = ((Integer)value).intValue();
            if(bottom < top){
              top = bottom;
            }
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
        },

        new FormProperty("TableLayoutConstraints.colSpan", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_colSpan"), // NOI18N
                         getBundle().getString("HINT_colSpan")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(right - left + 1);
          }
          public void setTargetValue(Object value){
            int colSpan = ((Integer)value).intValue();
            if(colSpan > 0){
              right = left + colSpan - 1;
            }
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
        },

        new FormProperty("TableLayoutConstraints.rowSpan", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_rowSpan"), // NOI18N
                         getBundle().getString("HINT_rowSpan")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(bottom - top + 1);
          }
          public void setTargetValue(Object value){
            int rowSpan = ((Integer)value).intValue();
            if(rowSpan > 0){
              bottom = top + rowSpan - 1;
            }
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
        },

        new FormProperty("TableLayoutConstraints.hAlign", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_hAlign"), // NOI18N
                         getBundle().getString("HINT_hAlign")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(hAlign);
          }
          public void setTargetValue(Object value){
            hAlign = ((Integer)value).intValue();
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
          public PropertyEditor getExpliciteEditor(){
            return new HAlignEditor();
          }
        },

        new FormProperty("TableLayoutConstraints.vAlign", // NOI18N
                         Integer.TYPE,
                         getBundle().getString("PROP_vAlign"), // NOI18N
                         getBundle().getString("HINT_vAlign")){ // NOI18N

          public Object getTargetValue(){
            return new Integer(vAlign);
          }
          public void setTargetValue(Object value){
            vAlign = ((Integer)value).intValue();
          }
          public void setPropertyContext(FormPropertyContext ctx){
            // disabling this method due to limited persistence
            // capabilities (compatibility with previous versions)
          } 
          public PropertyEditor getExpliciteEditor(){
            return new VAlignEditor();
          }
        },
      };
    }

    private void reinstateProperties(){
      try{
        for(int i = 0; i < properties.length; i++){
          FormProperty prop = (FormProperty) properties[i];
          prop.reinstateProperty();
        }
      }
      catch(IllegalAccessException e1) {} // should not happen
      catch(java.lang.reflect.InvocationTargetException e2) {} // should not happen
    }

    /** This method creates CodeExpression objects for properties of
     * AbsoluteConstraints - this is used by the layout delegate's method
     * createConstraintsCode which uses the expressions as parameters
     * in AbsoluteConstraints constructor.
     * @param codeStructure main CodeStructure object in which the code
     *        expressions are created
     * @param shift this parameter is used only by subclasses of
     *        AbsoluteLayoutConstraints (which may insert another
     *        constructor parameters before x, y, w and h)
     * @return array of created code expressions
     */
    protected final CodeExpression[] createPropertyExpressions(CodeStructure codeStructure){
      // first make sure properties are created...
      getProperties();
      
      // ...then create code expressions based on the properties
      CodeExpression xEl = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[0]));
      CodeExpression yEl = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[1]));
      CodeExpression wEl = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[2]));
      CodeExpression hEl = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[3]));
      CodeExpression hAlignEl = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[6]));
      CodeExpression vAlignEl = codeStructure.createExpression(FormCodeSupport.createOrigin(properties[7]));
      return new CodeExpression[]{xEl, yEl, wEl, hEl, hAlignEl, vAlignEl};
    }
    
    protected final void readCodeExpression(CodeExpression constrExp, CodeGroup codeGroup){
      // first make sure properties are created...
      getProperties();
      CodeExpression[] params = constrExp.getOrigin().getCreationParameters();
      FormCodeSupport.readPropertyExpression(params[0], properties[0], false);
      FormCodeSupport.readPropertyExpression(params[1], properties[1], false);
      FormCodeSupport.readPropertyExpression(params[2], properties[2], false);
      FormCodeSupport.readPropertyExpression(params[3], properties[3], false);
      FormCodeSupport.readPropertyExpression(params[4], properties[6], false);
      FormCodeSupport.readPropertyExpression(params[5], properties[7], false);
    }

  }

  public static final class HAlignEditor extends PropertyEditorSupport{
    private final String[] tags = {
      getBundle().getString("VALUE_hAlign_CENTER"),  // NOI18N
      getBundle().getString("VALUE_hAlign_FULL"),    // NOI18N
      getBundle().getString("VALUE_hAlign_LEADING"), // NOI18N
      getBundle().getString("VALUE_hAlign_LEFT"),    // NOI18N
      getBundle().getString("VALUE_hAlign_RIGHT"),   // NOI18N
      getBundle().getString("VALUE_hAlign_TRAILING") // NOI18N
    };
    private final Integer[] values = {
      new Integer(TableLayoutConstants.CENTER),
      new Integer(TableLayoutConstants.FULL),
      new Integer(TableLayoutConstants.LEADING),
      new Integer(TableLayoutConstants.LEFT),
      new Integer(TableLayoutConstants.RIGHT),
      new Integer(TableLayoutConstants.TRAILING)
    };
    private final String[] javaInitStrings = {
      "info.clearthought.layout.TableLayout.CENTER",  // NOI18N
      "info.clearthought.layout.TableLayout.FULL",    // NOI18N
      "info.clearthought.layout.TableLayout.LEADING", // NOI18N
      "info.clearthought.layout.TableLayout.LEFT",    // NOI18N
      "info.clearthought.layout.TableLayout.RIGHT",   // NOI18N
      "info.clearthought.layout.TableLayout.TRAILING" // NOI18N
    };
    
    public String[] getTags(){
      return tags;
    }
    
    public String getAsText(){
      Object value = getValue();
      for(int i=0; i < values.length; i++){
        if(values[i].equals(value)){
          return tags[i];
        }
      }
      return null;
    }
    
    public void setAsText(String str){
      for(int i=0; i < tags.length; i++){
        if(tags[i].equals(str)){
          setValue(values[i]);
        }
      }
    }
    
    public String getJavaInitializationString(){
      Object value = getValue();
      for (int i=0; i < values.length; i++){
        if (values[i].equals(value)){
          return javaInitStrings[i];
        }
      }
      return null;
    }
  }

  public static final class VAlignEditor extends PropertyEditorSupport{
    private final String[] tags = {
      getBundle().getString("VALUE_vAlign_BOTTOM"),  // NOI18N
      getBundle().getString("VALUE_vAlign_CENTER"),  // NOI18N
      getBundle().getString("VALUE_vAlign_FULL"),    // NOI18N
      getBundle().getString("VALUE_vAlign_TOP")      // NOI18N
    };
    private final Integer[] values = {
      new Integer(TableLayoutConstants.BOTTOM),
      new Integer(TableLayoutConstants.CENTER),
      new Integer(TableLayoutConstants.FULL),
      new Integer(TableLayoutConstants.TOP)
    };
    private final String[] javaInitStrings = {
      "info.clearthought.layout.TableLayout.BOTTOM",  // NOI18N
      "info.clearthought.layout.TableLayout.CENTER",  // NOI18N
      "info.clearthought.layout.TableLayout.FULL",    // NOI18N
      "info.clearthought.layout.TableLayout.TOP"      // NOI18N
    };
    
    public String[] getTags(){
      return tags;
    }
    
    public String getAsText(){
      Object value = getValue();
      for(int i=0; i < values.length; i++){
        if(values[i].equals(value)){
          return tags[i];
        }
      }
      return null;
    }
    
    public void setAsText(String str){
      for(int i=0; i < tags.length; i++){
        if(tags[i].equals(str)){
          setValue(values[i]);
        }
      }
    }
    
    public String getJavaInitializationString(){
      Object value = getValue();
      for (int i=0; i < values.length; i++){
        if (values[i].equals(value)){
          return javaInitStrings[i];
        }
      }
      return null;
    }
  }

  public static class DoubleArrayEditor extends PropertyEditorSupport implements XMLPropertyEditor{

    public String getAsText(){
      double[] value = (double[])getValue();
      NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
      numberFormat.setGroupingUsed(false);
      numberFormat.setMaximumFractionDigits(3);
      boolean firstValue = true;
      StringBuffer sb = new StringBuffer();
      for(int i = 0; i < value.length; i++){
        String sValue = null;
        if(value[i] == TableLayout.FILL){
          sValue = "FILL";
        }
        if(value[i] == TableLayout.MINIMUM){
          sValue = "MINIMUM";
        }
        if(value[i] == TableLayout.PREFERRED){
          sValue = "PREFERRED";
        }
        if((sValue == null) && (value[i] >= 0)){
          sValue = numberFormat.format(value[i]);
        }
        if(sValue != null){
          if(!firstValue){
            sb.append(",");
          }
          firstValue = false;
          sb.append(sValue);
        }
      }
      return sb.toString();
    }
    
    public void setAsText(String str){
      String[] tokens = str.split(",");
      double[] value = new double[tokens.length];
      int valueLength = 0;
      NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
      numberFormat.setGroupingUsed(false);
      numberFormat.setMaximumFractionDigits(3);
      for(int i = 0; i < tokens.length; i++){
        double dValue = 0;
        boolean hasValue = false;
        tokens[i] = tokens[i].trim();
        if(tokens[i].equalsIgnoreCase("FILL")){
          dValue = TableLayout.FILL;
          hasValue = true;
        }
        if(tokens[i].equalsIgnoreCase("MINIMUM")){
          dValue = TableLayout.MINIMUM;
          hasValue = true;
        }
        if(tokens[i].equalsIgnoreCase("PREFERRED")){
          dValue = TableLayout.PREFERRED;
          hasValue = true;
        }
        if(!hasValue){
          try{
            dValue = numberFormat.parse(tokens[i]).doubleValue();
            hasValue = true;
          }
          catch(Exception e){}
        }
        if(hasValue){
          value[valueLength++] = dValue;
        }
      }
      double[] rValue = new double[valueLength];
      System.arraycopy(value, 0, rValue, 0, valueLength);
      setValue(rValue);
    }
    
    public String getJavaInitializationString(){
      double[] value = (double[])getValue();
      NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
      numberFormat.setGroupingUsed(false);
      numberFormat.setMaximumFractionDigits(3);
      boolean firstValue = true;
      StringBuffer sb = new StringBuffer("new double[]{");
      for(int i = 0; i < value.length; i++){
        String sValue = null;
        if(value[i] == TableLayoutConstants.FILL){
          sValue = "info.clearthought.layout.TableLayout.FILL";
        }
        if(value[i] == TableLayoutConstants.MINIMUM){
          sValue = "info.clearthought.layout.TableLayout.MINIMUM";
        }
        if(value[i] == TableLayoutConstants.PREFERRED){
          sValue = "info.clearthought.layout.TableLayout.PREFERRED";
        }
        if((sValue == null) && (value[i] >= 0)){
          sValue = numberFormat.format(value[i]);
        }
        if(sValue != null){
          if(!firstValue){
            sb.append(",");
          }
          firstValue = false;
          sb.append(sValue);
        }
      }
      sb.append("}");
      return sb.toString();
    }
    
    /** Called to load property value from specified XML subtree. If succesfully loaded,
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     */
    public void readFromXML(org.w3c.dom.Node element) throws IOException{
      org.w3c.dom.Node vNode = element.getAttributes().getNamedItem("value");
      setAsText(vNode.getNodeValue());
    }

    /** Called to store current property value into XML subtree. The property value should be set using the
     * setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the value should be loaded
     */
    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc){
      org.w3c.dom.Element result = doc.createElement(getTag());
      result.setAttribute("value", getAsText());
      return result;
    }

    protected String getTag(){
      return "Array";
    }
    
  }

  public static class ColumnsEditor extends DoubleArrayEditor{
    
    protected String getTag(){
      return "Columns";
    }

  }

  public static class RowsEditor extends DoubleArrayEditor{
    
    protected String getTag(){
      return "Rows";
    }

  }

}
