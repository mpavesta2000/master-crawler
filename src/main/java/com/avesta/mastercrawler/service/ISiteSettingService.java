package com.avesta.mastercrawler.service;

import com.avesta.mastercrawler.model.SiteSetting;

import java.util.List;
import java.util.Optional;

public interface ISiteSettingService {
    Optional<SiteSetting> findById(Integer id);
    List<Integer> getBreakingNewsIds();
    void save(SiteSetting settings);
    List<SiteSetting> findAll();
    List<Integer> getNewsHeadlineIds();
    List<Integer> getMostViewedIds();
    List<Integer> getSliderIds();
}
