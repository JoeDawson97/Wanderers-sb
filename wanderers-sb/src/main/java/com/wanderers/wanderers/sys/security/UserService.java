package com.wanderers.wanderers.sys.security;

import com.wanderers.wanderers.app.dao.UserMapper;
import com.wanderers.wanderers.sys.base.BaseService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User,UserMapper> implements UserDetailsService {
    /**
     * 通过用户名获得用户对象
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User condition = new User();
        condition.setUsername(username);
        return this.find(condition);
    }
}
