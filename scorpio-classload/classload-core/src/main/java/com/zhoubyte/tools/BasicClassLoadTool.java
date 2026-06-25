package com.zhoubyte.tools;

import com.zhoubyte.Main;

import java.lang.reflect.Method;

public class BasicClassLoadTool {

    public static void basicInvokeClassload() {
        try{
            ClassLoader classLoader = Main.class.getClassLoader();
            System.out.println("Main classloader is " + classLoader);   // AppClassLoader
            Class<?> aClass = classLoader.loadClass("com.zhoubyte.plugins.CalcHouseAgent");
            System.out.println("目标类：" + aClass.getName());
            System.out.println("目标类加载器：" + aClass.getClassLoader()); // AppClassLoader
            System.out.println("目标类加载器父加载器：" + aClass.getClassLoader().getParent()); // PlatformClassLoader

            // 方式一
//            CalcHouseAgent abc = (CalcHouseAgent)aClass.getConstructor().newInstance();
//            System.out.println("直接初始化对象进行调用：" + abc.calc(1200D));

            // 方式二
            Method method = aClass.getMethod("calc", Double.class);
            Object invoke = method.invoke(aClass.getConstructor().newInstance(), 1200D);
            System.out.println("使用 Method 进行调用：" + invoke);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
