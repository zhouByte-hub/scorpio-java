package cglib_proxy;

import cglib_proxy.entity.UserEntity;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

// 实现MethodInterceptor接口，编写通用增强逻辑
public class CglibMethodInterceptor implements MethodInterceptor {

    @Override
    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        System.out.println(method.getName() + "方法触发");
        Object invoke = methodProxy.invokeSuper(o, objects);
        if(invoke instanceof UserEntity user){
            user.setPassword(encodePassword(user.getPassword()));
            return user;
        }
        return invoke;
    }


    private String encodePassword(String password) {
        if(password == null || password.isEmpty()) {
            return "未填写密码";
        }
        char[] charArray = password.toCharArray();
        char[] encodePassword = new char[charArray.length];
        for (int i = 0; i < charArray.length; i++) {
            encodePassword[i] = '*';
        }
        return new String(encodePassword);
    }
}
