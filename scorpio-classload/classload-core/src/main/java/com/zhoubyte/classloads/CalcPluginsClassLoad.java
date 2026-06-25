package com.zhoubyte.classloads;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureClassLoader;

/**
 * 专门用于加载 com.zhoubyte.plugins 包下的插件类。
 * - 只拦截 plugins 包下的类，走自定义加载逻辑（打破双亲委派）
 * - 其余类（java.*、com.zhoubyte.enums 等）仍委托父加载器，保证类型兼容
 */
public class CalcPluginsClassLoad extends SecureClassLoader{

    // .class 文件所在的根目录，指向编译产物 target/classes
    private final Path classRootDir;

    // 只有这个包下的类才走自定义加载
    private static final String PLUGINS_PACKAGE = "com.zhoubyte.plugins";

    // 不拦截接口类，接口始终委托父加载器以保证类型一致
    private static final String PLUGINS_INTERFACE = "com.zhoubyte.plugins.CalcInterface";

    public CalcPluginsClassLoad(String classRootDir, ClassLoader parent) {
        // 父加载器由调用方传入，确保与调用方使用的是同一个 ClassLoader
        super(parent);
        this.classRootDir = Paths.get(classRootDir).toAbsolutePath();
    }

    /**
     * 重写 loadClass：只对 plugins 包打破双亲委派，强制用自己加载。
     * 其余类仍走标准双亲委派，确保 CalcInterface 等接口由父加载器加载，强转不会报错。
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("触发 loadClass方法");
        synchronized (getClassLoadingLock(name)) {
            // 1. 先查本 ClassLoader 的缓存
            Class<?> cached = findLoadedClass(name);
            if (cached != null) {
                return cached;
            }

            // 2. plugins 包下的实现类 → 直接走自己的 findClass，不委托父加载器； 接口类 CalcInterface 除外，始终委托父加载器以保证类型一致
            if (name.startsWith(PLUGINS_PACKAGE) && !name.equals(PLUGINS_INTERFACE)) {
                return findClass(name);
            }

            // 3. 其他类 → 正常双亲委派
            return super.loadClass(name);
        }
    }

    /**
     * 核心：将类名转为文件路径，读取字节码，交给 JVM 完成后续流程。
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        System.out.println("触发 findClass 方法");
        // com.zhoubyte.plugins.CalcHouseAgent → com/zhoubyte/plugins/CalcHouseAgent.class
        String relativePath = name.replace('.', '/') + ".class";
        Path classFile = classRootDir.resolve(relativePath);

        byte[] classBytes;
        try {
            classBytes = Files.readAllBytes(classFile);
        } catch (IOException e) {
            throw new ClassNotFoundException("找不到插件类文件: " + classFile, e);
        }

        // 将字节码注册为 Class<?> 对象（JVM 负责验证 + 写入 Metaspace）
        return defineClass(name, classBytes, 0, classBytes.length);
    }

}
