package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.UsersType;

import java.util.List;
import java.util.Optional;

public interface IUsersTypeService {

    List<UsersType> findAll();
    Optional<UsersType> findById(Integer id);
}
