package com.avesta.mastercrawler.configuration;


import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.repository.*;
import com.avesta.mastercrawler.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

@Configuration
public class DataBaseSeeder {

    private final NewsTypeRepository newsTypeRepository;
    private final CategoryRepository categoryRepository;
    private final UsersTypeRepository usersTypeRepository;
    private final SiteSettingRepository siteSettingRepository;

    @Autowired
    public DataBaseSeeder(NewsTypeRepository newsTypeRepository, CategoryRepository categoryRepository, UsersTypeRepository usersTypeRepository, SiteSettingRepository siteSettingRepository) {
        this.newsTypeRepository = newsTypeRepository;
        this.categoryRepository = categoryRepository;
        this.usersTypeRepository = usersTypeRepository;
        this.siteSettingRepository = siteSettingRepository;
    }

    @Bean
    public CommandLineRunner seedDataBase(SiteSettingRepository siteSettingRepository) {
        return args -> {
            if(newsTypeRepository.findAll().isEmpty()) {
                newsTypeSeeder();
            }else {
                System.out.println("NewsTypes has been seeded before. Skipping seeding process.");
            }

            if(usersTypeRepository.findAll().isEmpty()) {
                usersTypeSeeder();
            }else {
                System.out.println("UsersType has been seeded before. Skipping seeding process.");
            }

            if(categoryRepository.findAll().isEmpty()) {
                categorySeeder();
            }else {
                System.out.println("Categories has been seeded before. Skipping seeding process.");
            }

            if(siteSettingRepository.findAll().isEmpty()) {
                siteSettingSeeder();
            }else {
                System.out.println("SiteSetting has been seeded before. Skipping seeding process.");
            }
        };
    }


    public void newsTypeSeeder() {
        ArrayList<NewsType> newsTypes = new ArrayList<>();

        newsTypes.add(new NewsType(null, "Image", new ArrayList<>()));
        newsTypes.add(new NewsType(null, "Video", new ArrayList<>()));
        newsTypes.add(new NewsType(null, "Default", new ArrayList<>()));

        newsTypeRepository.saveAll(newsTypes);
    }

    public void usersTypeSeeder() {
        ArrayList<UsersType> usersTypes = new ArrayList<>();

        usersTypes.add(new UsersType(null, "Admin", new ArrayList<>()));
        usersTypes.add(new UsersType(null, "User", new ArrayList<>()));
        usersTypes.add(new UsersType(null, "Ai", new ArrayList<>()));

        for (UsersType userType : usersTypes) {
            usersTypeRepository.save(userType);
        }
    }

    public void categorySeeder() {
        // Parent Categories
        Category sportsCategory = new Category(null, "ورزشی", null, null, null, "Active");
        categoryRepository.save(sportsCategory);

        Category defaultCategory = new Category(null, "اخبار", null, null, null, "Active");
        categoryRepository.save(defaultCategory);

        Category politicsCategory = new Category(null, "سیاست", null, null, null, "Active");
        categoryRepository.save(politicsCategory);

        Category entertainmentCategory = new Category(null, "فرهنگی و سرگرمی", null, null, null, "Active");
        categoryRepository.save(entertainmentCategory);

        Category businessCategory = new Category(null, "کسب و کار", null, null, null, "Active");
        categoryRepository.save(businessCategory);

        Category technologyCategory = new Category(null, "فناوری", null, null, null, "Active");
        categoryRepository.save(technologyCategory);

        Category healthCategory = new Category(null, "سلامت", null, null, null, "Active");
        categoryRepository.save(healthCategory);

        Category scienceCategory = new Category(null, "علم و دانش", null, null, null, "Active");
        categoryRepository.save(scienceCategory);

        Category lifestyleCategory = new Category(null, "سبک زندگی", null, null, null, "Active");
        categoryRepository.save(lifestyleCategory);

        Category worldCategory = new Category(null, "جهان", null, null, null, "Active");
        categoryRepository.save(worldCategory);

        Category educationCategory = new Category(null, "آموزش", null, null, null, "Active");
        categoryRepository.save(educationCategory);

        Category lawCategory = new Category(null, "قانون", null, null, null, "Active");
        categoryRepository.save(lawCategory);

        Category opinionCategory = new Category(null, "نظر", null, null, null, "Active");
        categoryRepository.save(opinionCategory);

        Category defaultSubCategory = new Category(null, "خبر", sportsCategory, null, null, "Active");
        categoryRepository.save(defaultSubCategory);

        // Subcategories (Examples for Sports)
        Category footballCategory = new Category(null, "فوتبال", sportsCategory, null, null, "Active");
        categoryRepository.save(footballCategory);

        Category basketballCategory = new Category(null, "بسکتبال", sportsCategory, null, null, "Active");
        categoryRepository.save(basketballCategory);

        Category tennisCategory = new Category(null, "تنیس", sportsCategory, null, null, "Active");
        categoryRepository.save(tennisCategory);

        // Subcategories for Politics
        Category localPoliticsCategory = new Category(null, "سیاست داخلی", politicsCategory, null, null, "Active");
        categoryRepository.save(localPoliticsCategory);

        Category internationalPoliticsCategory = new Category(null, "سیاست بین‌الملل", politicsCategory, null, null, "Active");
        categoryRepository.save(internationalPoliticsCategory);

        // Subcategories for Business
        Category financeCategory = new Category(null, "مالی", businessCategory, null, null, "Active");
        categoryRepository.save(financeCategory);

        Category marketsCategory = new Category(null, "بازارها", businessCategory, null, null, "Active");
        categoryRepository.save(marketsCategory);

        // Subcategories for Entertainment
        Category moviesCategory = new Category(null, "فیلم‌ها", entertainmentCategory, null, null, "Active");
        categoryRepository.save(moviesCategory);

        Category musicCategory = new Category(null, "موسیقی", entertainmentCategory, null, null, "Active");
        categoryRepository.save(musicCategory);

        Category celebrityCategory = new Category(null, "خبرهای سلبریتی‌ها", entertainmentCategory, null, null, "Active");
        categoryRepository.save(celebrityCategory);

        // Subcategories for Technology
        Category gadgetsCategory = new Category(null, "گجت‌ها", technologyCategory, null, null, "Active");
        categoryRepository.save(gadgetsCategory);

        Category cybersecurityCategory = new Category(null, "امنیت سایبری", technologyCategory, null, null, "Active");
        categoryRepository.save(cybersecurityCategory);

        Category softwareCategory = new Category(null, "نرم‌افزارها", technologyCategory, null, null, "Active");
        categoryRepository.save(softwareCategory);

        // Subcategories for Health
        Category fitnessCategory = new Category(null, "تناسب اندام", healthCategory, null, null, "Active");
        categoryRepository.save(fitnessCategory);

        Category nutritionCategory = new Category(null, "تغذیه", healthCategory, null, null, "Active");
        categoryRepository.save(nutritionCategory);

        Category mentalHealthCategory = new Category(null, "سلامت روان", healthCategory, null, null, "Active");
        categoryRepository.save(mentalHealthCategory);

        // Subcategories for Science
        Category biologyCategory = new Category(null, "زیست‌شناسی", scienceCategory, null, null, "Active");
        categoryRepository.save(biologyCategory);

        Category physicsCategory = new Category(null, "فیزیک", scienceCategory, null, null, "Active");
        categoryRepository.save(physicsCategory);

        Category chemistryCategory = new Category(null, "شیمی", scienceCategory, null, null, "Active");
        categoryRepository.save(chemistryCategory);

        // Subcategories for Lifestyle
        Category fashionCategory = new Category(null, "مد", lifestyleCategory, null, null, "Active");
        categoryRepository.save(fashionCategory);

        Category travelCategory = new Category(null, "سفر", lifestyleCategory, null, null, "Active");
        categoryRepository.save(travelCategory);

        Category foodCategory = new Category(null, "غذا", lifestyleCategory, null, null, "Active");
        categoryRepository.save(foodCategory);

        // Subcategories for World
        Category worldPoliticsCategory = new Category(null, "سیاست جهانی", worldCategory, null, null, "Active");
        categoryRepository.save(worldPoliticsCategory);

        Category globalHealthCategory = new Category(null, "سلامت جهانی", worldCategory, null, null, "Active");
        categoryRepository.save(globalHealthCategory);

        Category worldEconomyCategory = new Category(null, "اقتصاد جهانی", worldCategory, null, null, "Active");
        categoryRepository.save(worldEconomyCategory);

        // Subcategories for Education
        Category academicNewsCategory = new Category(null, "اخبار دانشگاهی", educationCategory, null, null, "Active");
        categoryRepository.save(academicNewsCategory);

        Category scholarshipsCategory = new Category(null, "بورسیه‌ها", educationCategory, null, null, "Active");
        categoryRepository.save(scholarshipsCategory);

        Category onlineLearningCategory = new Category(null, "یادگیری آنلاین", educationCategory, null, null, "Active");
        categoryRepository.save(onlineLearningCategory);

        // Subcategories for Law
        Category criminalLawCategory = new Category(null, "قانون کیفری", lawCategory, null, null, "Active");
        categoryRepository.save(criminalLawCategory);

        Category civilLawCategory = new Category(null, "قانون مدنی", lawCategory, null, null, "Active");
        categoryRepository.save(civilLawCategory);

        Category corporateLawCategory = new Category(null, "قانون تجارت", lawCategory, null, null, "Active");
        categoryRepository.save(corporateLawCategory);

        // Subcategories for Opinion
        Category editorialsCategory = new Category(null, "سرمقاله‌ها", opinionCategory, null, null, "Active");
        categoryRepository.save(editorialsCategory);

        Category guestColumnsCategory = new Category(null, "یادداشت مهمان‌ها", opinionCategory, null, null, "Active");
        categoryRepository.save(guestColumnsCategory);
    }

    public void siteSettingSeeder() {
        SiteSetting settings = new SiteSetting();
        siteSettingRepository.save(settings);
    }

}
