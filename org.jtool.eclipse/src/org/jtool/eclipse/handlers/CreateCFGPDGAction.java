/*
 *  Copyright 2015, Katsuhisa Maruyama (maru@jtool.org)
 */
 
package org.jtool.eclipse.handlers;

import org.jtool.eclipse.model.graph.GraphNodeIdPublisher;
import org.jtool.eclipse.model.java.JavaModelFactory;
import org.jtool.eclipse.model.java.JavaModelFactoryInWorkspace;
import org.jtool.eclipse.model.cfg.CFGFactory;
import org.jtool.eclipse.model.cfg.CFG;
import org.jtool.eclipse.model.pdg.PDGFactory;
import org.jtool.eclipse.model.pdg.PDG;
import org.jtool.eclipse.model.pdg.ClDGFactory;
import org.jtool.eclipse.model.pdg.ClDG;
import org.jtool.eclipse.model.pdg.SDGFactory;
import org.jtool.eclipse.model.pdg.SDG;
import org.jtool.eclipse.model.java.JavaASTDefaultVisitor;
import org.jtool.eclipse.model.java.JavaClass;
import org.jtool.eclipse.model.java.JavaProject;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jdt.core.IJavaProject;
import java.util.Set;

/**
 * Performs an action of creating CFGs and PDGs from source code within a project.
 * @author Katsuhisa Maruyama
 */
public class CreateCFGPDGAction extends JtoolHandler {
    
    /**
     * Executes a command with information obtained from the application context.
     * @param event an event containing all the information about the current state of the application
     * @return the result of the execution.
     * @throws ExecutionException if an exception occurred during execution
     */
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IJavaProject project = getJavaProject(event);
        if (project != null) {
            JavaProject.removeAllCache();
            GraphNodeIdPublisher.reset();
            
            JavaModelFactory factory = new JavaModelFactoryInWorkspace(project);
            factory.setJavaASTVisitor(new JavaASTDefaultVisitor());
            JavaProject jproject = factory.create();
            
            // createCFGs(jproject);
            // createPDGs(jproject);
            createSDG(jproject);
        }
        return null;
    }
    
    /**
     * Creates all CFGs for methods and fields with in the project.
     * @param the project
     */
    protected void createCFGs(JavaProject jproject) {
        CFGFactory.initialize();
        
        for (JavaClass jc : jproject.getJavaClassesInDictionaryOrder()) {
            Set<CFG> cfgs = CFGFactory.create(jc);
            
            CFGFactory.print(cfgs);
        }
    }
    
    /**
     * Creates all PDGs for methods and fields with in the project.
     * @param the project
     */
    protected void createPDGs(JavaProject jproject) {
        for (JavaClass jc : jproject.getJavaClassesInDictionaryOrder()) {
            Set<PDG> pdgs = PDGFactory.create(jc);
            
            PDGFactory.print(pdgs);
        }
    }
    
    /**
     * Creates all ClDGs for methods and fields with in the project.
     * @param the project
     */
    protected void createClDGs(JavaProject jproject) {
        for (JavaClass jc : jproject.getJavaClassesInDictionaryOrder()) {
            ClDG cldg = ClDGFactory.create(jc);
            
            ClDGFactory.print(cldg);
        }
    }
    
    /**
     * Creates all ClDGs for methods and fields within the project.
     * @param the project
     * @param name the method name
     */
    protected void createSDG(JavaProject jproject) {
        SDG sdg = SDGFactory.create(jproject.getJavaClassesInDictionaryOrder());
        
        SDGFactory.print(sdg);
    }
}
