package com.zhoubyte.tools;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarClassLoadTool {


//    public static void main(String[] args) throws Exception {
//        String path = "/Users/zhoujianing/Project/java/scorpio-classload/jarclassload/target/jarclassload-1.0-SNAPSHOT.jar";
//        Map<String, Class<?>> classes = JarClassLoadTool.scanJarClasses(path);
//        Class<?> aClass = classes.get("com.zhoubyte.Sum");
//        if(aClass==null){
//            throw new ClassNotFoundException("未找到对应的类");
//        }
//        Object o = aClass.getConstructor().newInstance();
//        // sum(Double... x) 是可变参数，反射调用时需要显式指定参数类型为 Double[].class
//        Method method = aClass.getMethod("sum", Double[].class);
//        // 可变参数在传给另一个可变参数方法时，如果不加 (Object) 强转，会被自动展开，导致参数数量不匹配。
//        Object invoke = method.invoke(o, (Object) new Double[]{1200D, 500D, 300D});
//        System.out.println(invoke);
//    }

    public static Map<String, Class<?>> scanJarClasses(String jarPath) throws Exception {
        Map<String, Class<?>> classMap = new HashMap<>();
        File jarFile = new File(jarPath);

        try (URLClassLoader loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
             JarFile jar = new JarFile(jarFile)) {

            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (name.endsWith(".class") && !name.startsWith("META-INF/")) {
                    String className = name.replace('/', '.').substring(0, name.length() - 6);
                    Class<?> clazz = loader.loadClass(className);
                    classMap.put(className, clazz);
                }
            }
        }catch (Exception e){
            throw new Exception(e);
        }
        return classMap;
    }
}
