package com.avesta.mastercrawler.restcontroller;

import com.avesta.mastercrawler.model.Comments;
import com.avesta.mastercrawler.service.ICommentsService;
import com.avesta.mastercrawler.service.INewsService;
import lombok.AllArgsConstructor;
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
public class CommentsDataRestController {

    private final ICommentsService iCommentsService;
    private final INewsService iNewsService;


    @GetMapping("/comments/data")
    @ResponseBody
    public Map<String, Object> getPaginatedComments(
            @RequestParam("draw") int draw,
            @RequestParam("start") int start,
            @RequestParam("length") int length,
            @RequestParam("search[value]") String searchValue,
            @RequestParam("order[0][column]") int orderColumn,
            @RequestParam("order[0][dir]") String orderDir) {

        String[] columnMapping = {"id", "name", "email", "content", "postedDate", "newsId", "status"};
        String orderBy = columnMapping[orderColumn];

        Pageable pageable = PageRequest.of(start / length, length,
                orderDir.equals("asc") ? Sort.by(orderBy).ascending() : Sort.by(orderBy).descending());

        Page<Comments> commentsPage = iCommentsService.findAllWithFilters(searchValue, pageable);

        List<Map<String, Object>> commentsData = commentsPage.getContent().stream().map(comment -> {
            Map<String, Object> data = new HashMap<>();
            data.put("id", comment.getId());
            data.put("name", comment.getName());
            data.put("email", comment.getEmail());
            data.put("content", comment.getContent());
            data.put("postedDate", comment.getCreatedAt());
            data.put("newsId", comment.getNews().getId());
            data.put("status", comment.isStatus());
            data.put("actions", "<div class='dropdown'>"
                    + "<button class='btn btn-link p-0 link-primary fs-15 dropdown-toggle' type='button' id='dropdownMenuButton' data-bs-toggle='dropdown' aria-expanded='false'>انتخاب وضیعت</button>"
                    + "<ul class='dropdown-menu' aria-labelledby='dropdownMenuButton'>"
                    + "<li><form action='/comments/changeStatus/" + comment.getId() + "' method='post'><button type='submit' class='dropdown-item'>"
                    + (comment.isStatus() ? "انتشار نشده" : "انتشار") + "</button></form></li>"
                    + "</ul></div>");
            data.put("erase", "<button class='btn btn-link p-0 link-danger fs-15' onclick='confirmDelete(" + comment.getId() + ")'><i class='ri-delete-bin-line'></i></button>");
            return data;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("draw", draw);
        response.put("recordsTotal", commentsPage.getTotalElements());
        response.put("recordsFiltered", commentsPage.getTotalElements());
        response.put("data", commentsData);

        return response;
    }
}
