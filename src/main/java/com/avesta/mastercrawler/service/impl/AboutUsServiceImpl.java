package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.AboutUs;
import com.avesta.mastercrawler.repository.AboutUsRepository;
import com.avesta.mastercrawler.service.IAboutUsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AboutUsServiceImpl implements IAboutUsService {

    private final AboutUsRepository aboutUsRepository;


    public List<AboutUs> findAll() {
        return aboutUsRepository.findAll();
    }

    public void save(AboutUs aboutUs) {
        aboutUsRepository.save(aboutUs);
    }

    public Optional<AboutUs> findById() {
        return aboutUsRepository.findById(1);
    }
}
