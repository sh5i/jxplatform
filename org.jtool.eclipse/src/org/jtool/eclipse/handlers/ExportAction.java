package org.jtool.eclipse.handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

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
        part = HandlerUtil.getActivePart(event);
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        
        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection structured = (IStructuredSelection) selection;
            
        if (structured.size() == 1) {
            IJavaProject project = toIJavaProject(structured.getFirstElement());
            if (project != null) {
                export(project);
            }
        } else {
            List<IJavaProject> projects = new ArrayList<IJavaProject>();
            for (Object elem : structured.toList()) {
                IJavaProject project = toIJavaProject(elem); 
                if (project != null) {
                    projects.add(project);
                }
            }
            export(projects);
        }
        return null;
    }

    /**
     * Converts a selection of Java project to IJavaProject. 
     */
    private IJavaProject toIJavaProject(Object o) {
        if (o instanceof IJavaProject) {
            return (IJavaProject) o;
        } else if (o instanceof IProject) {
            return (IJavaProject) JavaCore.create((IProject) o);
        } else {
            return null;
        }
    }

    /**
     * Exports multiple IJavaProject into JSON files.
     * @param projects the target set of IJavaProject
     */
    private void export(List<IJavaProject> projects) {
        String directory = askDirectory(null);
        if (directory != null) {
            for (IJavaProject project : projects) {
                String name = project.getProject().getName();
                exportTo(project, directory + File.separator + name + ".json");
            }
        }
    }

    /**
     * Exports an IJavaProject into a file as JSON.
     * @param project the target IJavaProject
     */
    private void export(IJavaProject project) {
        String filename = askFilename(project.getProject().getName() + ".json", "json");
        if (filename != null) {
            exportTo(project, filename);
        }
    }
    
    /**
     * Exports an IJavaProject into a file as JSON.
     * @param project the target IJavaProject
     * @param filename the JSON file to store
     */
    private void exportTo(IJavaProject project, String filename) {
        JavaProject.removeAllCache();
        GraphNodeIdPublisher.reset();
        
        JavaModelFactory factory = new JavaModelFactoryInWorkspace(project);
        factory.setJavaASTVisitor(new JavaASTDefaultVisitor());
        JavaProject jproject = factory.create();
        String json = JSON.encode(AttributeExtractor.toMap(jproject), true);
        
        save(json, filename);
    }
    
    /**
     * Ask a filename using FileDialog.
     * @param defaultName default name which will be shown in the dialog.
     * @param ext extension without including dot
     * @return the input filename or null when cancelled
     */
    protected String askFilename(final String defaultName, final String ext) {
        Display display = Display.getCurrent();
        Shell shell = new Shell(display); 
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFileName(defaultName);
        dialog.setFilterExtensions(new String[] { "*." + ext });
        return dialog.open();
    }
    
    /**
     * Ask a filename using FileDialog.
     * @param defaultName default name which will be shown in the dialog.
     * @return the specified directory or null when cancelled
     */
    protected String askDirectory(final String defaultName) {
        Display display = Display.getCurrent();
        Shell shell = new Shell(display);
        DirectoryDialog dialog = new DirectoryDialog(shell, SWT.SAVE);
        return dialog.open();
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
