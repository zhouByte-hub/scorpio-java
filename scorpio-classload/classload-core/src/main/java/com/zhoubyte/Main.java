package com.zhoubyte;

import com.zhoubyte.tools.CustomClassLoadTool;
import com.zhoubyte.tools.HotReloadTool;
import com.zhoubyte.tools.JarClassLoadTool;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


/**
 * 1. 类加载器的作用是将 class 文件加载到 JVM 中（findClass -> loadClass）。
 * 2. 类加载器的生命周期与整个应用的生命周期相同，但是加载的类一样会走：加载->连接->初始化->使用->卸载->销毁的流程。
 * 3. JVM 遵循懒加载（Lazy Loading）原则，依赖类不是在加载主类时就全部加载，而是在真正用到的那一刻才加载；依赖类由"引用它的那个类的 ClassLoader"来加载。
 */

public class Main {


    public static void main(String[] args) throws Exception {
        hotReloadDemo();
//        CustomClassLoadTool.loadClassByCustomClassLoader();
    }

    /**
     * 热加载演示
     */
    public static void hotReloadDemo() throws Exception {
        // 动态获取 classRootDir（适配多模块项目）
        Path classRootDir = Paths.get(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        
        HotReloadTool hotReload = new HotReloadTool(classRootDir.toString(), Main.class.getClassLoader());
        
        // 1. 初始加载
        hotReload.initialLoad();
        
        // 2. 启动文件监控（每 2 秒检查一次）
        hotReload.startWatch(6000);

        // 3. 主线程循环调用，演示热重载效果
        int round = 1;
        while (true) {
            Thread.sleep(6000);
            Object result = hotReload.invokePlugin("CalcHouseAgent", 1200D);
            System.out.println("[Round " + round + "] CalcHouseAgent 结果: " + result);
            System.out.println("  当前 ClassLoader: " + hotReload.getCurrentLoader());
            round++;
        }
    }

    /**
     * 静态加载（原来的逻辑）
     */
    public static void staticLoad() throws Exception {
        String path = "/Users/zhoujianing/Project/java/scorpio-classload/jarclassload/target/jarclassload-1.0-SNAPSHOT.jar";
        Map<String, Class<?>> classes = JarClassLoadTool.scanJarClasses(path);
        Class<?> aClass = classes.get("com.zhoubyte.Sum");
        if(aClass==null){
            throw new ClassNotFoundException("未找到对应的类");
        }
        Object o = aClass.getConstructor().newInstance();
        // sum(Double... x) 是可变参数，反射调用时需要显式指定参数类型为 Double[].class
        Method method = aClass.getMethod("sum", Double[].class);
        // 可变参数在传给另一个可变参数方法时，如果不加 (Object) 强转，会被自动展开，导致参数数量不匹配。
        Object invoke = method.invoke(o, (Object) new Double[]{1200D, 500D, 300D});
        System.out.println(invoke);
    }






}