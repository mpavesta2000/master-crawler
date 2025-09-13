package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface IUsersService {

    Optional<Users> findByEmail(String email);
    void save(Users user);
    void savePassword(Users users);
    Optional<Users> findById(Integer id);
    List<Users> findAll();
    Page<Users> findAllAdminsWithFilters(String searchValue, Pageable pageable);
    Page<Users> findAllAdmins(Pageable pageable);
}
