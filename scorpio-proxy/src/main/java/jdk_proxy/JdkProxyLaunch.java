package jdk_proxy;

import jdk_proxy.entity.UserProxy;
import jdk_proxy.interfaces.JdkProxyInterface;

import java.lang.reflect.Proxy;

/**
 * JDK动态代理是Java语言提供的一种基于接口的代理机制，允许开发者在运行时动态地创建代理对象，而无需为每个类 编写具体的代理实现。
 * 这种机制主要通过 java.lang.reflect.Proxy 类和 java.lang.reflect.InvocationHandler 接口实现。
 * 使用步骤
 *      1. 定义接口：首先定义一个或多个接口，代理对象将实现这些接口。
 *      2. 实现接口：创建一个类，它实现上述接口，提供具体的实现逻辑。
 *      3. 创建 InvocationHandler 实现：定义一个 InvocationHandler 的实现，这个实现中的 invoke 方法可以包含自定义逻辑。
 *      4. 创建代理对象：使用 Proxy.newProxyInstance 方法，传入目标对象的类加载器、需要代理的接口数组以及 InvocationHandler 的实现，来创建一个实现了指定接口的代理对象。
 */
public class JdkProxyLaunch {

    /**
     * 每当对代理对象执行方法调用时，调用的方法不会直接执行，而是转发到实现了InvocationHandler 的 invoke 方法上。在这个 invoke 方法内部，我们可以定义拦截逻辑、调用原始对象的方法、修改返回值等操作。
     * 1. 类加载器，需要与代理类的类加载器是同一个类加载器
     * 2. 代理对象实现的接口
     * 3. 代理执行器
     *      3.1 代理对象
     *      3.2 调用的方法
     *      3.3 方法的参数
     */
    public static void main(String[] args) {
        UserProxy userProxy = new UserProxy();

        // 方式一
        JdkProxyInterface userProxyImpl1 = (JdkProxyInterface) Proxy.newProxyInstance(
                JdkProxyLaunch.class.getClassLoader(), new Class[]{JdkProxyInterface.class},
                (proxy, method, args1) -> {
                    if(method.getName().equals("initUserProxy")) {
                        System.out.println("代理方式一：触发了 initUserProxy");
                    }
                    return method.invoke(userProxy, args1);
                });

        userProxyImpl1.initUserProxy();

        // 方式二
        JdkProxyInterface userProxyImpl2 = (JdkProxyInterface) Proxy.newProxyInstance(
                JdkProxyLaunch.class.getClassLoader(),
                new Class[]{JdkProxyInterface.class},
                new AdvancedInvocationHandler(userProxy));

        userProxyImpl2.initUserProxy();
    }
}
