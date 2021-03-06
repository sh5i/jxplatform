/*
 *  Copyright 2014, Katsuhisa Maruyama (maru@jtool.org)
 */

package org.jtool.eclipse.model.java;

import org.jtool.eclipse.io.FileReader;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Stores information of a Java source file.
 * @author Katsuhisa Maruyama
 */
public class JavaFile {
    
    static Logger logger = Logger.getLogger(JavaFile.class.getName());
    
    /**
     * A compilation unit corresponding to this file.
     */
    protected ICompilationUnit compilationUnit;
    
    /**
     * The path name of this file.
     */
    protected String path;
    
    /**
     * A project containing this file.
     */
    protected JavaProject jproject;
    
    /**
     * The collection of errors during the generation of this file.
     */
    protected List<IProblem> errors;
    
    /**
     * Creates a new, empty object.
     */
    JavaFile() {
        super();
    }
    
    /**
     * Creates an object that will store information about a given file.
     * @param icu the compilation unit.
     * @param jproject the project containing this file
     */
    public JavaFile(ICompilationUnit icu, JavaProject jproject) {
        this.compilationUnit = icu;
        this.path = icu.getPath().toString();
        this.jproject = jproject;
    }
    
    /**
     * Creates an object that will store information about a given file.
     * @param path the path name of the file
     * @param jproject the project containing this file
     */
    public JavaFile(String path, JavaProject jproject) {
        this.path = path;
        this.jproject = jproject;
        
    }
    
    /**
     * Returns the path name of this file
     * @return the path name
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Returns the project containing this file.
     * @return the project
     */
    public JavaProject getJavaProject() {
        return jproject;
    }
    
    /**
     * Obtains the relative path name of a given file or directory to a base directory. 
     * @param path the path name of the file or directory
     * @param base the path name of the base directory
     * @return the relative pathname, or <code>null</code> if the file or directory is not contained a base directory or its sub-directories
     */
    public static String getRelativePath(String path, String base) {
        if (path.startsWith(base) && path.length() > base.length()) {
            return path.substring(base.length() + 1);
        }
        return null;
    }
    
    /**
     * Obtains the contents of the current source code for this file.
     * @return the contents of the source code
     */
    public String getSource() {
        try {
            if (compilationUnit != null) {
                return compilationUnit.getSource();
            }
            String name = getFilePath(jproject.getTopDir());
            return FileReader.read(name);
            
        } catch (IOException e) {
        } catch (JavaModelException e) {
        }
        
        return "";
    }
    
    /**
     * Obtains the path of the source code stored in this file.
     * @param progName the project name
     * @return the file name of the source code
     */
    private String getFilePath(String projName) {
        int lindex = projName.lastIndexOf(File.separatorChar);
        String progDir = projName.substring(0, lindex);
        
        return progDir + path;
    }
    
    /**
     * Sets the collection of errors during the generation of this file.
     * @param errors the parse errors
     */
    public void setParseErrors(List<IProblem> errors) {
        this.errors = errors;
    }
    
    /**
     * Returns the collection of errors during the generation of this file.
     * @return the parse errors
     */
    public List<IProblem> getParseErrors() {
        return errors;
    }
    
    /**
     * Displays the parse errors.
     */
    public void printParseErrors() {
        if (errors != null && errors.size() > 0) {
            for (IProblem problem : errors) {
                logger.debug("problem: " + problem.getMessage() + problem.getSourceStart());
            }
        }
    }
    
    /**
     * Tests if a given class or interface equals to this.
     * @param obj the Java class
     * @return <code>true</code> if the given class equals to this, otherwise <code>false</code>
     */
    public boolean equals(Object obj) {
        if (obj instanceof JavaFile) {
            JavaFile jcu = (JavaFile)obj;
            return equals(jcu);
        }
        return false;
    }
    
    /**
     * Tests if a given class or interface equals to this.
     * @param jc the Java class
     * @return <code>true</code> if the given class equals to this, otherwise <code>false</code>
     */
    public boolean equals(JavaFile jcu) {
        if (jcu == null) {
            return false;
        }
        
        if (this == jcu) {
            return true;
        }
        
        return getPath().compareTo(jcu.getPath()) == 0; 
    }
    
    /**
     * Returns a hash code value for this file.
     * @return the hash code value for the file
     */
    public int hashCode() {
        return getPath().hashCode();
    }
    
    /**
     * Collects information about this file.
     * @return the string for printing
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("FILE: ");
        buf.append(getPath());
        buf.append("\n");
        
        return buf.toString();
    }
}
