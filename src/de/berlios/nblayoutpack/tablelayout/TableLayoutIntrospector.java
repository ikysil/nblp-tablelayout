/*
 * TableLayoutIntrospector.java
 *
 * Created on December 23, 2004, 11:37 PM
 */

package de.berlios.nblayoutpack.tablelayout;

import java.awt.*;
import java.lang.reflect.*;

import org.openide.ErrorManager;

import info.clearthought.layout.*;

/**
 *
 * @author  Illya Kysil
 */
public class TableLayoutIntrospector{

  private static Constructor constrConstructor;
  private static Constructor layoutConstructor;
  private static Method setHGapMethod;
  private static Method setVGapMethod;
  private static Method setColumnMethod;
  private static Method setRowMethod;
  private static Method calculateSizeMethod;
  private static Field crSizeField;
  private static Field crOffsetField;

  /** Creates a new instance of TableLayoutIntrospector */
  private TableLayoutIntrospector(){
  }
  
  public static Constructor getConstraintsConstructor(){
    if(constrConstructor == null){
      try{
        constrConstructor = TableLayoutConstraints.class.getConstructor(
            new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE,
                        Integer.TYPE, Integer.TYPE});
      }
      catch(NoSuchMethodException e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return constrConstructor;
  }
  
  public static Constructor getLayoutConstructor(){
    if(layoutConstructor == null){
      try{
        layoutConstructor = TableLayout.class.getConstructor(new Class[0]);
      }
      catch(NoSuchMethodException e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return layoutConstructor;
  }
  
  public static Method getSetHGapMethod(){
    if(setHGapMethod == null){
      try{
        setHGapMethod = TableLayout.class.getMethod("setHGap", new Class[]{int.class});
      }
      catch(NoSuchMethodException e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return setHGapMethod;
  }
  
  public static Method getSetVGapMethod(){
    if(setVGapMethod == null){
      try{
        setVGapMethod = TableLayout.class.getMethod("setVGap", new Class[]{int.class});
      }
      catch(NoSuchMethodException e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return setVGapMethod;
  }
  
  public static Method getSetColumnMethod(){
    if(setColumnMethod == null){
      try{
        setColumnMethod = TableLayout.class.getMethod("setColumn", new Class[]{double[].class});
      }
      catch(NoSuchMethodException e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return setColumnMethod;
  }
  
  public static Method getSetRowMethod(){
    if(setRowMethod == null){
      try{
        setRowMethod = TableLayout.class.getMethod("setRow", new Class[]{double[].class});
      }
      catch(NoSuchMethodException e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    return setRowMethod;
  }
  
  public static int[][] getCrSize(TableLayout tableLayout){
    if(crSizeField == null){
      try{
        crSizeField = TableLayout.class.getDeclaredField("crSize");
        crSizeField.setAccessible(true);
      }
      catch(Exception e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    int[][] result = null;
    try{
      if(crSizeField == null){
        return result;
      }
      result = (int[][])crSizeField.get(tableLayout);
    }
    catch(Exception e){
      ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
    }
    return result;
  }
  
  public static int[][] getCrOffset(TableLayout tableLayout){
    if(crOffsetField == null){
      try{
        crOffsetField = TableLayout.class.getDeclaredField("crOffset");
        crOffsetField.setAccessible(true);
      }
      catch(Exception e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    int[][] result = null;
    try{
      if(crOffsetField == null){
        return result;
      }
      result = (int[][])crOffsetField.get(tableLayout);
    }
    catch(Exception e){
      ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
    }
    return result;
  }
  
  public static void calculateSize(TableLayout tableLayout, Container container){
    if(calculateSizeMethod == null){
      try{
        calculateSizeMethod = TableLayout.class.getDeclaredMethod("calculateSize", new Class[]{Container.class});
        calculateSizeMethod.setAccessible(true);
      }
      catch(Exception e){
        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
      }
    }
    try{
      if(calculateSizeMethod == null){
        return;
      }
      calculateSizeMethod.invoke(tableLayout, new Object[]{container});
    }
    catch(Exception e){
      ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
    }
  }
  
}
