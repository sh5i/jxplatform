package org.jtool.eclipse.handlers;

import java.io.IOException;

import net.arnx.jsonic.JSON;

import org.jtool.eclipse.io.FileWriter;
import org.jtool.eclipse.io.JtoolFile;
import org.jtool.eclipse.model.graph.GraphNodeIdPublisher;
import org.jtool.eclipse.model.java.AttributeExtractor;
import org.jtool.eclipse.model.java.JavaASTDefaultVisitor;
import org.jtool.eclipse.model.java.JavaModelFactory;
import org.jtool.eclipse.model.java.JavaModelFactoryInWorkspace;
import org.jtool.eclipse.model.java.JavaProject;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * Performs an action of parsing source code within a project and saving as JSON.
 * @author Shinepi Hayashi
 * @author Katsuhisa Maruyama
 */
public class ExportAction extends JtoolHandler {
    
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
            save(project.getProject(), jproject);
        }
        return null;
    }
    
    /**
     * Saves information on given classes.
     * @param project the project containing the classes
     * @param classes the collection of the classes
     */
    protected void save(IProject project, JavaProject jproject) {
        Display display = Display.getCurrent();
        Shell shell = new Shell(display); 
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFileName(project.getName() + ".json");
        dialog.setFilterExtensions(new String[] { "*.json" });
        String filename = dialog.open();
        if (filename != null) {
            save(JSON.encode(AttributeExtractor.toMap(jproject), true), filename);
        }
    }
    
    /**
     * Saves information into a file.
     * @param jc the text to write
     * @param filename the file name of the file
     */
    protected void save(String text, String filename) {
        try { 
            JtoolFile savefile = new JtoolFile(filename);
            savefile.makeDir();
            FileWriter.write(savefile, text);
            logger.info("save file: " + filename);
        } catch (IOException e) {
            printMessage(part, "Save error: " + filename);
        }
    }
}
