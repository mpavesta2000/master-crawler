package com.avesta.mastercrawler.controller;

import com.avesta.mastercrawler.service.rss.AlWatanRSSChecker;
import com.avesta.mastercrawler.service.rss.BBCArabicRSSChecker;
import com.avesta.mastercrawler.service.rss.BornaRSSChecker;
import com.avesta.mastercrawler.service.rss.FararuRSSChecker;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/rss")
@AllArgsConstructor
public class RssController {

    private final BornaRSSChecker bornaRSSChecker;
    private final FararuRSSChecker fararuRSSChecker;
    private final BBCArabicRSSChecker bbcArabicRSSChecker;
    private final AlWatanRSSChecker alWatanRSSChecker;

    @GetMapping("/setting")
    public String loggingPage(Model model) {
        model.addAttribute("bornaRSSEnabled", bornaRSSChecker.isEnabled());
        model.addAttribute("defaultBornaMaxNews", bornaRSSChecker.getMaxNews());

        model.addAttribute("fararuRSSEnabled", fararuRSSChecker.isEnabled());
        model.addAttribute("defaultFararuMaxNews", fararuRSSChecker.getMaxNews());

        model.addAttribute("bbcArabicRSSEnabled", bbcArabicRSSChecker.isEnabled());
        model.addAttribute("defaultBBCArabicMaxNews", bbcArabicRSSChecker.getMaxNews());

        model.addAttribute("alWatanRSSEnabled", alWatanRSSChecker.isEnabled());
        model.addAttribute("defaultAlWatanMaxNews", alWatanRSSChecker.getMaxNews());

        return "rss/rss-setting";
    }
}
