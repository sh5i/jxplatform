package org.jtool.eclipse.model.java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * A converter of JxPlatform Java model to a Map object.
 * @author Shinpei Hayashi
 */
public class AttributeExtractor {
    static Logger logger = Logger.getLogger(AttributeExtractor.class.getName());
    
    public static Map<String, Object> toMap(JavaProject jp) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", jp.getName());
        result.put("directory", jp.getTopDir());
        List<Object> classes = new ArrayList<Object>();
        for (JavaClass jc : jp.getJavaClassesInDictionaryOrder()) {
            classes.add(toMap(jc));
        }
        result.put("classes", classes);
        return result;
    }

    public static Map<String, Object> toMap(JavaElement je) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("start_position", je.getStartPosition());
        result.put("length", je.getCodeLength());
        result.put("upper_line_number", je.getUpperLineNumber());
        result.put("bottom_line_number", je.getBottomLineNumber());
        if (je.getAnnotations().size() > 0) {
            List<Object> annotations = new ArrayList<Object>();
            for (JavaAnnotation ja : je.getAnnotations()) {
                annotations.add(toMap(ja));
            }
            result.put("annotations",  annotations);
        }
        return result;
    }

    public static Map<String, Object> toMap(JavaClass jc) {
        Map<String, Object> result = toMap((JavaElement) jc);
        result.put("name", jc.getName());
        result.put("package", jc.getJavaPackage().getName());
        result.put("file", jc.getJavaFile().getPath());
        String superClassName = jc.getSuperClassName();
        if (superClassName != null && !superClassName.equals("java.lang.Object")) {
            result.put("extends", superClassName);
        }
        if (!jc.getSuperInterfaceNames().isEmpty()) {
            result.put("implements", new ArrayList<>(jc.getSuperInterfaceNames()));
        }

        List<Object> classes = new ArrayList<Object>();
        for (JavaClass c : jc.getJavaInnerClasses()) {
            if (c == jc) {
                // Self-loop detected. 
                // TODO why happens?
                continue;
            }
            classes.add(toMap(c));
        }
        result.put("inner_classes", classes);

        List<Object> fields = new ArrayList<Object>();
        for (JavaField jf : jc.getJavaFieldsInDictionaryOrder()) {
            fields.add(toMap(jf));
        }
        result.put("fields", fields);
        
        List<Object> methods = new ArrayList<Object>();
        for (JavaMethod jm : jc.getJavaMethodsInDictionaryOrder()) {
            methods.add(toMap(jm));
        }
        result.put("methods", methods);
        return result;
    }

    public static Map<String, Object> toMap(JavaAnnotation ja) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("name", ja.getName());
        result.put("values", ja.getValues());
        return result;
    }

    public static Map<String, Object> toMap(JavaField jf) {
        Map<String, Object> result = toMap((JavaElement) jf);
        result.put("name", jf.getName());
        result.put("type", jf.getType());
        return result;
    }
    
    public static Map<String, Object> toMap(JavaMethod jm) {
        Map<String, Object> result = toMap((JavaElement) jm);
        result.put("signature", jm.getSignature());
        result.put("type", jm.getReturnType());
        List<Object> locals = new ArrayList<Object>();
        for (JavaLocal jl : jm.locals) {
            locals.add(toMap(jl));
        }
        result.put("locals", locals);
        return result;
    }
    
    public static Map<String, Object> toMap(JavaLocal jl) {
        Map<String, Object> result = toMap((JavaElement) jl);
        result.put("name", jl.getName());
        result.put("type", jl.getType());
        return result;
    }
}
