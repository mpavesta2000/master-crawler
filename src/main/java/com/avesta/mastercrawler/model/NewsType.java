package com.avesta.mastercrawler.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "news_type")
public class NewsType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_type_id")
    private Integer newsTypeId;

    @Column(name = "news_type_name")
    private String newsTypeName;

    @OneToMany(mappedBy = "newsTypeId",cascade = CascadeType.ALL)
    private List<News> news;

    public NewsType() {
    }

    public NewsType(Integer newsTypeId, String newsTypeName, List<News> news) {
        this.newsTypeId = newsTypeId;
        this.newsTypeName = newsTypeName;
        this.news = news;
    }

    public Integer getNewsTypeId() {
        return newsTypeId;
    }

    public void setNewsTypeId(Integer newsTypeId) {
        this.newsTypeId = newsTypeId;
    }

    public String getNewsTypeName() {
        return newsTypeName;
    }

    public void setNewsTypeName(String newsTypeName) {
        this.newsTypeName = newsTypeName;
    }

    public List<News> getNews() {
        return news;
    }

    public void setNews(List<News> news) {
        this.news = news;
    }

    @Override
    public String toString() {
        return "NewsType{" +
                "newsTypeId=" + newsTypeId +
                ", newsTypeName='" + newsTypeName + '\'' +
                ", news=" + news +
                '}';
    }
}
