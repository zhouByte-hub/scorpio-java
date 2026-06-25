package com.zhoubyte.tools;

import com.zhoubyte.Main;
import com.zhoubyte.classloads.CalcPluginsClassLoad;
import com.zhoubyte.plugins.CalcInterface;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class CustomClassLoadTool {

    public static void loadClassByCustomClassLoader() {
        // 从 Main.class 的实际加载路径反推出 target/classes 目录，适配多模块项目
        Path classRootDir;
        try {
            classRootDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (Exception e) {
            throw new RuntimeException("无法定位编译产物目录", e);
        }

        String pluginsPackage = "com.zhoubyte.plugins";
        Path pluginsDir = classRootDir.resolve(pluginsPackage.replace('.', '/'));
        System.out.println("classRootDir: " + classRootDir);
        System.out.println("pluginsDir:   " + pluginsDir);

        CalcPluginsClassLoad loader = new CalcPluginsClassLoad(classRootDir.toString(), Main.class.getClassLoader());
        try (Stream<Path> classFiles = Files.list(pluginsDir)) {
            // 扫描 plugins 目录下所有 .class 文件，过滤掉接口文件
            classFiles.filter(p -> p.toString().endsWith(".class")).forEach(classFile -> {
                // CalcHouseAgent.class → com.zhoubyte.plugins.CalcHouseAgent
                String fileName = classFile.getFileName().toString();
                String simpleClassName = fileName.replace(".class", "");
                String fullClassName = pluginsPackage + "." + simpleClassName;
                try {
                    Class<?> clazz = loader.loadClass(fullClassName);

                    // 过滤接口本身，只处理实现了 CalcInterface 的类
                    if (!CalcInterface.class.isAssignableFrom(clazz) || clazz.isInterface()) {
                        return;
                    }

                    CalcInterface plugin = (CalcInterface) clazz.getConstructor().newInstance();
                    System.out.println("["+simpleClassName+"] ClassLoader: " + clazz.getClassLoader());
                    System.out.println("["+simpleClassName+"] 计算结果: " + plugin.calc(1200D));
                } catch (Exception e) {
                    throw new RuntimeException("加载插件失败: " + fullClassName, e);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
