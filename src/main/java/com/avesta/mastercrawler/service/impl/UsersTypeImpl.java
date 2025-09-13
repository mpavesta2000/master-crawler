package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.UsersType;
import com.avesta.mastercrawler.repository.UsersTypeRepository;
import com.avesta.mastercrawler.service.IUsersTypeService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersTypeImpl implements IUsersTypeService {

    private final UsersTypeRepository usersTypeRepository;

    public UsersTypeImpl(UsersTypeRepository usersTypeRepository) {
        this.usersTypeRepository = usersTypeRepository;
    }

    @Override
    public List<UsersType> findAll() {
        return usersTypeRepository.findAll();
    }

    @Override
    public Optional<UsersType> findById(Integer id) {
        Optional<UsersType> usersType = usersTypeRepository.findById(id);
        if(usersType.isPresent()) {
            return usersType;
        }
        return Optional.empty();
    }
}
