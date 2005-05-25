package de.berlios.nblayoutpack.tablelayout;

import org.openide.modules.ModuleInstall;

import org.netbeans.modules.form.layoutsupport.LayoutSupportRegistry;

/** Manages a module's lifecycle.
 * Remember that an installer is optional and often not needed at all.
 *
 * @author Illya Kysil
 */
public class TableLayoutModule extends ModuleInstall{
  
  public void restored(){
    LayoutSupportRegistry.registerSupportForLayout(
      "info.clearthought.layout.TableLayout",
      "de.berlios.nblayoutpack.tablelayout.TableLayoutSupport"
    );
  }
  
  // Less commonly needed:
    /*
    public boolean closing() {
        // return false if cannot close now
        return true;
    }
    public void close() {
        // shut down stuff
    }
     */
  
  // Generally the methods below should be avoided in favor of restored():
    /*
    // By default, do nothing but call restored().
    public void installed() {
  restored();
    }
     
    // By default, do nothing.
    public void uninstalled() {
    }
     
    // By default, call restored().
    public void updated(int release, String specVersion) {
    }
     */
  
  // It is no longer recommended to override Externalizable methods
  // (readExternal and writeExternal). See the Modules API section on
  // "installation-clean" modules for an explanation.
  
}
