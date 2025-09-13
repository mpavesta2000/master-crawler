package com.avesta.mastercrawler.repository;

import com.avesta.mastercrawler.model.SiteSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SiteSettingRepository extends JpaRepository<SiteSetting, Integer> {

}
