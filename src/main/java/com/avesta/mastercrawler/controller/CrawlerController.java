package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.dto.VoiceDto;
import com.avesta.mastercrawler.model.*;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.utility.DataScope;
import com.avesta.mastercrawler.utility.FileUploadUtil;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/quickNews")
@AllArgsConstructor
public class CrawlerController {

    private FileUploadUtil fileUploadUtil;
    private final INewsService iNewsService;

    @GetMapping("/crawler")
    public String showCrawler(Model model) {
        Map<String, String> supportedSites = new HashMap<>();
        supportedSites.put("افکار نیوز", "www.afkarnews.com");
        supportedSites.put("آفتاب یزد آنلاین", "www.aftabyazdonline.ir");
        supportedSites.put("الأهرام", "ahram.org.eg");
        supportedSites.put("الجزیرة", "www.aljazeera.net");
        supportedSites.put("المیادین", "www.almayadeen.net");
        supportedSites.put("الوطن", "www.al-watan.com");
        supportedSites.put("الاهرام", "www.gate.ahram.org.eg");
        supportedSites.put("العربیة", "www.alarabiya.net");
        supportedSites.put("بی بی سی عربی", "www.bbc.com/arabic");
        supportedSites.put("الف", "www.alef.ir");
        supportedSites.put("آرمان ملی", "www.armanmeli.ir");
        supportedSites.put("آینده نیوز", "www.ayandnews.com");
        supportedSites.put("آنا", "www.ana.press");
        supportedSites.put("آلامتو", "www.alamto.com");
        supportedSites.put("آفتاب نیوز", "www.aftabnews.ir");
        supportedSites.put("ارز دیجیتال", "www.arzdigital.com");
        supportedSites.put("عصر ایران", "www.asriran.com");
        supportedSites.put("بانک ملی ایران", "www.bmi.ir");
        supportedSites.put("برترین‌ها", "www.bartarinha.ir");
        supportedSites.put("بانک صادرات ایران", "www.bsi.ir");
        supportedSites.put("بانک کشاورزی", "www.bki.ir");
        supportedSites.put("بانک پاسارگاد", "www.bpi.ir");
        supportedSites.put("بازده", "www.bazdeh.org");
        supportedSites.put("بیمه آسیا", "www.bimehasia.com");
        supportedSites.put("بانک سپه", "www.banksepah.ir");
        supportedSites.put("برنا", "www.borna.news");
        supportedSites.put("بورس ۲۴", "www.bourse24.ir");
        supportedSites.put("چی بپوشم", "www.chibepoosham.com");
        supportedSites.put("بانک مرکزی ایران", "www.cbi.ir");
        supportedSites.put("چطور", "www.chetor.com");
        supportedSites.put("سینما برتر", "www.cinemabartar.ir");
        supportedSites.put("چرخان", "www.charkhan.com");
        supportedSites.put("دیلی فوتبال", "www.dailyfootball.tv");
        supportedSites.put("دانا", "www.dana.ir");
        supportedSites.put("بیمه دی", "www.dayins.com");
        supportedSites.put("دلگرم", "www.delgarm.com");
        supportedSites.put("دیجی کالا", "www.digikala.com");
        supportedSites.put("دفاع پرس", "www.defapress.ir");
        supportedSites.put("دیجیاتو", "www.digiato.com");
        supportedSites.put("دز امروز", "www.deztoday.ir");
        supportedSites.put("بیمه دانا", "www.dana-insurance.com");
        supportedSites.put("دنیای اقتصاد", "www.donya-e-eqtesad.com");
        supportedSites.put("اکو ایران", "www.ecoiran.com");
        supportedSites.put("بانک اقتصاد نوین", "www.enbank.ir");
        supportedSites.put("اتحادیه طلا و جواهر", "www.estjt.ir");
        supportedSites.put("انصاف نیوز", "www.ensafnews.com");
        supportedSites.put("اطلاعات", "www.ettelaat.com");
        supportedSites.put("اکو نیوز", "www.econews.ir");
        supportedSites.put("بانک توسعه صادرات ایران", "www.edbi.ir");
        supportedSites.put("اقتصاد آنلاین", "www.eghtesadonline.com");
        supportedSites.put("اقتصاد ۱۰۰", "www.eghtesad100.ir");
        supportedSites.put("اقتصاد نیوز", "www.eghtesadnews.com");
        supportedSites.put("انتخاب", "www.entekhab.ir");
        supportedSites.put("فرارو", "www.fararu.com");
        supportedSites.put("فردا نیوز", "www.fardanews.com");
        supportedSites.put("فردای اقتصاد", "www.fardayeeghtesad.com");
        supportedSites.put("فارس نیوز", "www.farsnews.ir");
        supportedSites.put("فرتاک نیوز", "www.fartaknews.com");
        supportedSites.put("فوتبالی", "www.footballi.net");
        supportedSites.put("گجت نیوز", "www.gadgetnews.net");
        supportedSites.put("جی اس ام", "www.gsm.ir");
        supportedSites.put("حادثه ۲۴", "www.hadese24.ir");
        supportedSites.put("همشهری آنلاین", "www.hamshahrionline.ir");
        supportedSites.put("بیمه حافظ", "www.hafezinsurance.ir");
        supportedSites.put("حوزه نیوز", "www.hawzahnews.com");
        supportedSites.put("هنر زندگی", "www.honarehzendegi.com");
        supportedSites.put("بانک ایران زمین", "www.izbank.ir");
        supportedSites.put("ایپنا", "www.ipna.ir");
        supportedSites.put("بیمه ایران", "www.iraninsurance.ir");
        supportedSites.put("ایانا", "www.iana.ir");
        supportedSites.put("ایلنا", "www.ilna.ir");
        supportedSites.put("اقتصاد ایران", "www.iraneconomist.com");
        supportedSites.put("دیپلماسی ایرانی", "www.irdiplomacy.ir");
        supportedSites.put("ایرنا", "www.irna.ir");
        supportedSites.put("ایسنا", "www.isna.ir");
        supportedSites.put("ایونا", "www.iwna.ir");
        model.addAttribute("supportedSites", supportedSites);
        return "crawler/chapchin";
    }

    @GetMapping("/generate")
    public String generateNews() {
        return "crawler/generate-based-question";
    }

    @GetMapping("/text")
    public String voiceToText(Model model) {
        String directoryPath = "news/voices";
        File folder = new File(directoryPath);

        List<VoiceDto> voiceDtoList = new ArrayList<>();

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        double sizeInMB = file.length() / (1024.0 * 1024.0);
                        double roundedSize = Math.round(sizeInMB * 1000.0) / 1000.0;
                        voiceDtoList.add(new VoiceDto(file.getName(), roundedSize));
                    }
                }
            }
        }

        model.addAttribute("voiceList", voiceDtoList);
        return "crawler/voice-to-text";
    }


//    @PostMapping("/upload/voice")
//    public String uploadVoice(@RequestParam(required = false) MultipartFile voices, RedirectAttributes redirectAttributes) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (!(authentication instanceof AnonymousAuthenticationToken)) {
//            if (voices != null && !voices.isEmpty() && voices.getSize() > 0) {
//                System.out.println(voices);
//                try {
//                    String uploadDir = "images/news/voices";
//                    String filename = voices.getOriginalFilename();
//
//                    fileUploadUtil.saveFile(uploadDir, filename, voices);
//
//                    redirectAttributes.addFlashAttribute("success", true);
//                } catch (IOException e) {
//                    redirectAttributes.addFlashAttribute("error", "مشکل در آپلود فایل:" + e.getMessage());
//                }
//            } else {
//                redirectAttributes.addFlashAttribute("error", "هیچ فایلی ارسال نشده است");
//            }
//        }
//        return "redirect:/quickNews/text";
//    }

    @GetMapping("/translate")
    public String translateNews(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "18") int size,
                                @RequestParam(required = false) String search) {

        PageRequest pageRequest = PageRequest.of(page, size);
        // null for Admin/User (all news); the Ai user's own email otherwise
        Page<News> newsPage = iNewsService.searchNews(search, DataScope.ownerEmailFilter(), pageRequest);

        model.addAttribute("news", newsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newsPage.getTotalPages());
        model.addAttribute("totalItems", newsPage.getTotalElements());
        model.addAttribute("searchQuery", search);
        return "crawler/translate-news";
    }


}

