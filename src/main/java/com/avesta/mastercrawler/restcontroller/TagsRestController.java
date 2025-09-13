package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.Tags;
import com.avesta.mastercrawler.service.INewsService;
import com.avesta.mastercrawler.service.ITagsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class TagsRestController {

    private final INewsService iNewsService;
    private final ITagsService iTagsService;

    @GetMapping("/tags/search")
    public ResponseEntity<List<String>> searchTags(@RequestParam String query) {
        List<String> matchedTags = iTagsService.findTagsByQuery(query);
        return ResponseEntity.ok(matchedTags);
    }

    @GetMapping("/tags/data")
    @ResponseBody
    public Map<String, Object> getPaginatedTags(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam("search[value]") String searchValue,
            @RequestParam("order[0][column]") int orderColumn,
            @RequestParam("order[0][dir]") String orderDir) {

        String[] columnMapping = {"id", "name"};
        String orderBy = columnMapping[orderColumn];

        Pageable pageable = PageRequest.of(start / length, length,
                orderDir.equals("asc") ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());

        Page<Tags> tagsPage = iTagsService.findAllWithFilters(searchValue, pageable);

        List<Map<String, Object>> tagsData = tagsPage.getContent().stream().map(tags -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", tags.getId());
            data.put("name", tags.getName());
            data.put("actions", "<button class='btn btn-link p-0 link-danger fs-15' onclick='confirmDelete(" + tags.getId() + ")'><i class='ri-delete-bin-line'></i></button>");
            return data;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", tagsPage.getTotalElements());
        response.put("recordsFiltered", tagsPage.getTotalElements());
        response.put("data", tagsData);

        return response;
    }




}
