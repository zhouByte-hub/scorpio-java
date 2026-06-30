package cglib_proxy;

import cglib_proxy.entity.UserEntity;
import cglib_proxy.service.UserService;

public class CglibProxyLaunch {

    public static void main(String[] args) {
        UserService userService = ProxyFactory.newProxyInstance(UserService.class);
        UserEntity user = userService.createUser("张三", "123123", "zhangsan@dayu.com");
        System.out.println(user);
    }
}
