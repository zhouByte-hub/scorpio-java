package com.zhoubyte.tools;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

public class UrlClassLoadTool {

    public static void loadClassByUrl() throws IOException {
        URLClassLoader urlClassLoader = null;
        try {
            URL url = Paths.get(System.getProperty("java.class.path").split(":")[0]).toAbsolutePath().toUri().toURL();
            urlClassLoader = new URLClassLoader(new URL[]{url});
            System.out.println("UrlClassLoader: " + urlClassLoader);

            Class<?> aClass = urlClassLoader.loadClass("com.zhoubyte.plugins.CalcHouseAgent");
            System.out.println("CalcAgent ClassLoader: " + aClass.getClassLoader());

            /*
                URLClassLoader 与 AppClassLoader 是不同的 ClassLoader，
                跨 ClassLoader 直接强转容易导致 ClassCastException；
                不能直接强转 CalcAgent，应使用反射调用
             */
            Method method = aClass.getMethod("calc", Double.class);
            Object result = method.invoke(aClass.getConstructor().newInstance(), 1200D);
            System.out.println("结果: " + result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (urlClassLoader != null) {
                urlClassLoader.close();
            }
        }
    }
}
