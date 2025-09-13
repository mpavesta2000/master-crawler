package com.avesta.mastercrawler.service.impl;

import com.avesta.mastercrawler.model.SiteSetting;
import com.avesta.mastercrawler.repository.SiteSettingRepository;
import com.avesta.mastercrawler.service.ISiteSettingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class SiteSettingServiceImpl implements ISiteSettingService {

    private final SiteSettingRepository siteSettingRepository;

    public Optional<SiteSetting> findById(Integer id) {
        return siteSettingRepository.findById(id);
    }

    public List<Integer> getBreakingNewsIds() {
        Optional<SiteSetting> breakingNewsList = siteSettingRepository.findById(1);
        if(breakingNewsList.isPresent()) {
            return breakingNewsList.get().getBreakingNews();
        }else {
            return new ArrayList<>();
        }
    }

    public void save(SiteSetting settings) {
        siteSettingRepository.save(settings);
    }

    public List<SiteSetting> findAll() {
        return siteSettingRepository.findAll();
    }

    public List<Integer> getNewsHeadlineIds() {
        Optional<SiteSetting> siteSetting = findById(1);
        return siteSetting.map(SiteSetting::getNewsHeadline).orElse(new ArrayList<>());
    }

    public List<Integer> getMostViewedIds() {
        Optional<SiteSetting> siteSetting = findById(1);
        return siteSetting.map(SiteSetting::getShowMostViewed).orElse(new ArrayList<>());
    }

    public List<Integer> getSliderIds() {
        Optional<SiteSetting> siteSetting = findById(1);
        return siteSetting.map(SiteSetting::getSlider).orElse(new ArrayList<>());
    }

}
