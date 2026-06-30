package cglib_proxy.service;

import cglib_proxy.entity.UserEntity;

public class UserService {

    public UserEntity createUser(String username, String password, String email) {
        return UserEntity.of(username, password, email);
    }
}
