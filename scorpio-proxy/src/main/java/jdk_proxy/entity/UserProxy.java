package jdk_proxy.entity;

import jdk_proxy.interfaces.JdkProxyInterface;

public class UserProxy implements JdkProxyInterface {

    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public UserProxy initUserProxy() {
        return null;
    }
}
