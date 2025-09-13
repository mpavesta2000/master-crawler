package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.CallUs;

import java.util.List;
import java.util.Optional;

public interface ICallUsService {
    List<CallUs> findAll();
    void save(CallUs callUs);
    Optional<CallUs> findById();
}
