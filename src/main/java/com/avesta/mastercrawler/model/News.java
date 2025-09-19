package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "news")
@Getter
@Setter
@ToString(exclude = "categories")
@AllArgsConstructor
@NoArgsConstructor
public class News extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = true, columnDefinition = "TEXT")
    @Lob
    @NotNull(message = "تیتر خبر الزامی است")
    @Size(min = 5, message = "تیتر خبر باید بیشتر ۵ کاراکتر باشد")
    private String title;

    @Column(name = "news_lead", nullable = false, columnDefinition = "TEXT")
    @Lob
    @NotNull(message = "سرنخ خبر الزامی است")
    private String lead;

    @Column(name = "sub_title", columnDefinition = "TEXT")
    @Lob
    private String subTitle;

    @Column(name = "headline", columnDefinition = "TEXT")
    @Lob
    private String headline;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "news_tags",
            joinColumns = @JoinColumn(name = "news_id"),
            inverseJoinColumns = @JoinColumn(name = "tags_id")
    )
    private List<Tags> tags;

    @Column(name = "meta_title", columnDefinition = "TEXT")
    @Lob
    private String metaTitle;

    @Column(name = "meta_description", columnDefinition = "TEXT")
    @Lob
    private String metaDescription;

    @Column(name = "meta_keywords", columnDefinition = "TEXT")
    @Lob
    private String metaKeywords;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    @Lob
    @NotNull(message = "متن خبر الزامی است")
    private String body;

    @Column(name = "main_image", nullable = true)
    @Lob
    private String mainImage;

    @ElementCollection
    @CollectionTable(name = "news_images", joinColumns = @JoinColumn(name = "news_id"))
    @Column(name = "image_url")
    private List<String> imageList = new ArrayList<>();

    @Column(name = "main_video", nullable = true)
    private String mainVideo;

    @Column(name = "breaking_news")
    private Boolean breakingNews;

    @Column(name = "news_headline")
    private Boolean newsHeadline;

    @Column(name = "show_comments")
    private Boolean showComments;

    @Column(name = "show_most_viewed")
    private Boolean showMostViewed;

    @Column(name = "slider")
    private Boolean slider;

    @Column(name = "get_translated")
    private Boolean getTranslated;

    @Column(name = "status", nullable = false)
    @NotNull(message = "وضعیت الزامی است")
    private String status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "news_type_id")
    private NewsType newsTypeId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "news_categories",
            joinColumns = @JoinColumn(name = "news_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @NotNull(message = "دسته بندی الزامی است")
    private List<Category> categories;

    @OneToMany(mappedBy = "news", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Comments> comments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private Users userId;

    @Column(name = "slug", columnDefinition = "TEXT")
    @Lob
    private String slug;

    @Column(name = "focus_keyphrase", columnDefinition = "TEXT")
    @Lob
    private String focusKeyphrase;

    @Column(name = "seo_title", columnDefinition = "TEXT")
    @Lob
    private String seoTitle;




}
