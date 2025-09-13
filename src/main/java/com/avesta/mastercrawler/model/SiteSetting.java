package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "site_setting")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SiteSetting extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ElementCollection
    @CollectionTable(name = "breaking_news", joinColumns = @JoinColumn(name = "site_setting_id"))
    @Column(name = "news_id")
    private List<Integer> breakingNews;

    @ElementCollection
    @CollectionTable(name = "slider", joinColumns = @JoinColumn(name = "site_setting_id"))
    @Column(name = "news_id")
    private List<Integer> slider;

    @ElementCollection
    @CollectionTable(name = "show_most_viewed", joinColumns = @JoinColumn(name = "site_setting_id"))
    @Column(name = "news_id")
    private List<Integer> showMostViewed;

    @ElementCollection
    @CollectionTable(name = "news_headline", joinColumns = @JoinColumn(name = "site_setting_id"))
    @Column(name = "news_id")
    private List<Integer> newsHeadline;


    @Override
    public String toString() {
        return "SiteSetting{" +
                "id=" + id +
                ", breakingNews=" + breakingNews +
                ", slider=" + slider +
                ", showMostViewed=" + showMostViewed +
                ", newsHeadline=" + newsHeadline +
                '}';
    }

}
