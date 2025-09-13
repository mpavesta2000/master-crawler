package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.CallUs;
import com.avesta.mastercrawler.repository.CallUsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CallUsServiceImpl {

    private final CallUsRepository callUsRepository;

    public List<CallUs> findAll() {
        return callUsRepository.findAll();
    }

    public void save(CallUs callUs) {
        callUsRepository.save(callUs);
    }

    public Optional<CallUs> findById() {
        return callUsRepository.findById(1);
    }
}
