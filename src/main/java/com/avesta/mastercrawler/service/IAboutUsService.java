package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.AboutUs;

import java.util.List;
import java.util.Optional;

public interface IAboutUsService {
    List<AboutUs> findAll();
    void save(AboutUs aboutUs);
    Optional<AboutUs> findById();
}
