package com.cumulations.music_player.service;

import com.cumulations.music_player.dto.entity.UserInfo;
import com.cumulations.music_player.repo.UserInfoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailService implements UserDetailsService {
    private UserInfoRepo userRepo;

    @Autowired
    public CustomUserDetailService(UserInfoRepo userRepo) {
        this.userRepo = userRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo userInfo = userRepo.findByEmail(email)
                .orElseThrow(()->new UsernameNotFoundException("User with email: "+email+" Don't exist, please Sign up before login"));

        Set<GrantedAuthority> authorities = userInfo
                .getRoles()
                .stream()
                .map((role) -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toSet());

        return new User(userInfo.getEmail(),userInfo.getPassword(),true,true,true,userInfo.isActive(),authorities);
    }
}
