package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Users;
import com.avesta.mastercrawler.repository.UsersRepository;
import com.avesta.mastercrawler.utility.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Autowired
    public CustomUserDetailsService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
       Users user =  usersRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: "));
        return new CustomUserDetails(user);
    }
}
