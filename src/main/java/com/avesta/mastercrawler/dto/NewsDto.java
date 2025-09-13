package com.avesta.mastercrawler.dto;

import com.avesta.mastercrawler.model.Category;
import com.avesta.mastercrawler.model.Users;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NewsDto {

    private Integer newsId;

    private String newsHeadLine;

    private String newsLead;

    private String newsBody;

    private Users userId;

    private List<Category> categories;

    private String status;

    private String newsType;


    @Override
    public String toString() {
        return "NewsDto{" +
                "newsId=" + newsId +
                ", newsHeadLine='" + newsHeadLine + '\'' +
                ", newsLead='" + newsLead + '\'' +
                ", newsBody='" + newsBody + '\'' +
                ", userId=" + userId +
                ", categories=" + categories +
                ", status='" + status + '\'' +
                ", newsType='" + newsType + '\'' +
                '}';
    }
}
