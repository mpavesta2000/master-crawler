package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.Category;
import com.avesta.mastercrawler.model.News;
import com.avesta.mastercrawler.service.INewsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class NewsDataRestController {

    private final INewsService iNewsService;

    @GetMapping("/data")
    @ResponseBody
    public Map<String, Object> getPaginatedNews(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam("search[value]") String searchValue,
            @RequestParam(value = "statusFilter", required = false) String statusFilter,
            @RequestParam(value = "newsTypeFilter", required = false) String newsTypeFilter,
            @RequestParam(value = "userFilter", required = false) String userFilter,
            @RequestParam(value = "idFilter", required = false) String idFilter,
            @RequestParam("order[0][column]") int orderColumn,
            @RequestParam("order[0][dir]") String orderDir) {

        Integer idFilterInt = null;
        if (idFilter != null && !idFilter.trim().isEmpty()) {
            try {
                idFilterInt = Integer.parseInt(idFilter.trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid idFilter value: " + idFilter);
                idFilterInt = null;
            }
        }

        String[] columnMapping = {"id", "title", "newsTypeId.newsTypeName", "userId.email", "postedDate", "status", "categories"};
        String orderBy = columnMapping[orderColumn];

        Pageable pageable = PageRequest.of(start / length, length,
                orderDir.equals("desc") ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());

        Page<News> newsPage = iNewsService.findAllWithFilters(searchValue, statusFilter, newsTypeFilter, userFilter, idFilterInt ,pageable);

        List<Map<String, Object>> newsData = newsPage.getContent().stream().map(news -> {
            Map<String, Object> data = new HashMap<>();
            data.put("newsId", news.getId());
            data.put("title", news.getTitle());
            data.put("newsType", news.getNewsTypeId().getNewsTypeName());
            data.put("author", news.getUserId().getEmail());
            data.put("createdAt", news.getCreatedAt());
            data.put("status", news.getStatus().equals("Draft") ? "پیش نویس" : "منتشر شده");
            data.put("categories", news.getCategories().stream().map(Category::getName).collect(Collectors.joining(", ")));
            data.put("actions", "<a href='/admin/news/edit/" + news.getId() + "' class='link-success fs-15'><i class='ri-edit-2-line'></i></a>" +
                    "<button class='btn btn-link p-0 link-danger fs-15' onclick='confirmDelete(" + news.getId() + ")'><i class='ri-delete-bin-line'></i></button>");
            return data;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", newsPage.getTotalElements());
        response.put("recordsFiltered", newsPage.getTotalElements());
        response.put("data", newsData);

        return response;
    }

}
