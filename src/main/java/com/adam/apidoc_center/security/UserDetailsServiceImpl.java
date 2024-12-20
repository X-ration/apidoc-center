package com.adam.apidoc_center.security;

import com.adam.apidoc_center.domain.User;
import com.adam.apidoc_center.domain.UserAuthority;
import com.adam.apidoc_center.repository.UserAuthorityRepository;
import com.adam.apidoc_center.repository.UserRepository;
import com.adam.apidoc_center.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("=======执行自定义登录逻辑====");
        User user;
        if(StringUtil.isNumber(username)) {
            long userId = Long.parseLong(username);
            user = userRepository.findById(userId);
        } else if(StringUtil.isEmail(username)) {
            user = userRepository.findByEmail(username);
        } else {
            user = userRepository.findByUsername(username);
        }
        if(user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
//        List<UserAuthority> userAuthorities = userAuthorityRepository.findByUserId(user.getId());
        List<UserAuthority> userAuthorities = user.getUserAuthorities();
        userAuthorities = userAuthorities == null ? new ArrayList<>() : userAuthorities;
        List<GrantedAuthority> grantedAuthorities = userAuthorities.stream()
                .map(userAuthority -> new SimpleGrantedAuthority(userAuthority.getAuthority().name()))
                .collect(Collectors.toList());
        return new ExtendedUser(user.getUsername(), user.getPassword(), grantedAuthorities, user);
    }
}
