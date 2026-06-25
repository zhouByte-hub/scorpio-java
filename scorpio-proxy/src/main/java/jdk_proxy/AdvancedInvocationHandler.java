package jdk_proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class AdvancedInvocationHandler implements InvocationHandler {

    private final Object target;

    public AdvancedInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("initUserProxy")) {
            System.out.println("代理方式二：触发了 initUserProxy");
        }
        return method.invoke(target, args);
    }
}
