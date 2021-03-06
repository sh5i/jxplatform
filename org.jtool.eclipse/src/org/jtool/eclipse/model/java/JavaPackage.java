/*
 *  Copyright 2014, Katsuhisa Maruyama (maru@jtool.org)
 */

package org.jtool.eclipse.model.java;

import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.IPackageBinding;
import java.util.Set;
import java.util.HashSet;
import org.apache.log4j.Logger;

/**
 * An object representing a package.
 * @author Katsuhisa Maruyama
 */
public class JavaPackage {
    
    static Logger logger = Logger.getLogger(JavaPackage.class.getName());
    
    /**
     * The name of this package.
     */
    protected String name;
    
    /**
     * A project containing this package.
     */
    protected JavaProject jproject;
    
    /**
     * The collection of all classes within this package.
     */
    protected Set<JavaClass> classes = new HashSet<JavaClass>();
    
    /**
     * Creates a new, empty object.
     */
    JavaPackage() {
        super();
    }
    
    /**
     * Creates a new object representing a package.
     * @param name the name of the package
     * @param jp the project containing the package
     */
    private JavaPackage(String name, JavaProject jp) {
        this.name = name;
        jproject = jp;
    }
    
    /**
     * Creates a new object representing a package.
     * @param node an AST node for this package
     * @param jp the project containing this package
     * @return the package object
     */
    public static JavaPackage create(PackageDeclaration node, JavaProject jp) {
        String name = ".UNKNOWN";
        if (node != null) {
            IPackageBinding binding = node.resolveBinding();
            if (binding != null) {
                name = binding.getName();
            }
        } else {
            name = ".DEFAULT";
        }
        
        JavaPackage jpackage = jp.getJavaPackage(name);
        if (jpackage != null) {
            return jpackage;
        }
        
        jpackage = new JavaPackage(name, jp);
        jp.addJavaPackage(jpackage);
        return jpackage;
    }
    
    /**
     * Creates a new object representing a package.
     * @param name the name of the package
     * @param jp the project containing the package
     * @return the package object
     */
    public static JavaPackage create(String name, JavaProject jp) {
        JavaPackage jpackage = jp.getJavaPackage(name);
        if (jpackage != null) {
            return jpackage;
        }
        
        jpackage = new JavaPackage(name, jp);
        jp.addJavaPackage(jpackage);
        return jpackage;
    }
    
    /**
     * Tests if this package exists in the project.
     * @return always <code>true</code>
     */
    public boolean isInProject() {
        return true;
    }
    
    /**
     * Returns the project containing this package.
     * @return the project containing this package
     */
    public JavaProject getJavaProject() {
        return jproject;
    }
    
    /**
     * Returns the name of this package.
     * @return the name string
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the name of the project containing this package.
     * @return the name string
     */
    public String getProjectName() {
        return jproject.getName();
    }
    
    /**
     * Adds a specified class to the members of this package.
     * @param jm the class to be added
     */
    public void addJavaClass(JavaClass jc) {
        classes.add(jc);
    }
    
    /**
     * Returns all the classes within this class or interface.
     * @return the collection of the classes declared in this class or interface
     */
    public Set<JavaClass> getJavaClasses() {
        return classes;
    }
    
    /**
     * Returns a class having a specified name.
     * @param fqn the full-qualified name of the class to be retrieved
     * @return the found class, or <code>null</code> if none
     */
    public JavaClass getJavaClass(String fqn) {
        for (JavaClass jc : classes) {
            if (jc.getQualifiedName().compareTo(fqn) == 0) {
                return jc;
            }
        }
        return null;
    }
    
    /**
     * Tests if a given package equals to this.
     * @param obj the Java package
     * @return <code>true</code> if the given package equals to this, otherwise <code>false</code>
     */
    public boolean equals(Object obj) {
        if (obj instanceof JavaPackage) {
            JavaPackage jp = (JavaPackage)obj;
            return equals(jp);
        }
        return false;
    }
    
    /**
     * Tests if a given package equals to this.
     * @param jp the Java package
     * @return <code>true</code> if the given package equals to this, otherwise <code>false</code>
     */
    public boolean equals(JavaPackage jp) {
        if (jp == null) {
            return false;
        }
        
        if (this == jp) {
            return true;
        }
        
        return getProjectName().compareTo(jp.getProjectName()) == 0 && getName().compareTo(jp.getName()) == 0; 
    }
    
    /**
     * Returns a hash code value for this package.
     * @return the hash code value for the package
     */
    public int hashCode() {
        return getName().hashCode();
    }
    
    /**
     * Collects information about this package.
     * @return the string for printing
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("PACKAGE: ");
        buf.append(getName());
        buf.append("\n");
        
        return buf.toString();
    }
    
    /* ================================================================================
     * The following functionalities can be used after completion of whole analysis 
     * ================================================================================ */
    
    /**
     * The collection of all packages that depend on the classes within this package.
     */
    protected Set<JavaPackage> afferentPackages = new HashSet<JavaPackage>();
    
    /**
     * The collection of all packages that the classes within this package depend on.
     */
    protected Set<JavaPackage> efferentPackages = new HashSet<JavaPackage>();
    
    /**
     * A flag that indicates all bindings for package relationships were found.
     */
    protected boolean bindingOk = false;
    
    /**
     * Collects additional information on this package.
     */
    public void collectLevel3Info() {
        bindingOk = true;
        
        for (JavaClass jc : classes) {
            for (JavaClass c : jc.getEfferentJavaClassesInProject()) {
                JavaPackage jp = c.getJavaPackage();
                efferentPackages.add(jp);
                
                if (!c.isBindingOk()) {
                    bindingOk = false;
                }
            }
        }
        
        for (JavaPackage jp : efferentPackages) {
            jp.addAfferentPackage(this);
        }
    }
    
    /**
     * Adds a package that depends on classes within this package.
     * @param jm the afferent package
     */
    private void addAfferentPackage(JavaPackage jp) {
        if (!afferentPackages.contains(jp)) {
            afferentPackages.add(jp);
        }
    }
    
    /**
     * Tests if the binding for this package was found.
     * @return <code>true</code> if the binding was found
     */
    public boolean isBindingOk() {
        return bindingOk;
    }
    
    /**
     * Displays error log if the binding is not completed.
     */
    private void bindingCheck() {
        if (!bindingOk) {
            logger.info("This API can be called after the completion of whole analysis");
        }
    }
    
    /**
     * Returns all the packages that depend on classes within this package.
     * @return the collection of the afferent packages 
     */
    public Set<JavaPackage> getAfferentJavaPackages() {
        bindingCheck();
        return afferentPackages;
    }
    
    /**
     * Returns all the packages that depend on classes within this package.
     * @return the collection of the afferent packages in the project 
     */
    public Set<JavaPackage> getAfferentJavaPackagesInProject() {
        bindingCheck();
        
        Set<JavaPackage> packages = new HashSet<JavaPackage>();
        for (JavaPackage jp : getAfferentJavaPackages()) {
            if (jp.isInProject()) {
                packages.add(jp);
            }
        }
        return packages;
    }
    
    /**
     * Returns all the packages that the classes within this package depend on.
     * @return the collection of the efferent packages 
     */
    public Set<JavaPackage> getEfferentJavaPackages() {
        bindingCheck();
        return efferentPackages;
    }
    
    /**
     * Returns all the packages that the classes within this package depend on.
     * @return the collection of the efferent packages in the project
     */
    public Set<JavaPackage> getEfferentJavaPackagesInProject() {
        bindingCheck();
        
        Set<JavaPackage> packages = new HashSet<JavaPackage>();
        for (JavaPackage jp : getEfferentJavaPackages()) {
            if (jp.isInProject()) {
                packages.add(jp);
            }
        }
        return packages;
    }
}
