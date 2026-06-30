package cglib_proxy;

import net.sf.cglib.proxy.Enhancer;

public class ProxyFactory {

    public static <T> T newProxyInstance(Class<T> clazz) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new CglibMethodInterceptor());
        return (T) enhancer.create();
    }
}
