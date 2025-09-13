package com.avesta.mastercrawler.model;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "links")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Links extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image")
    private String image;

    @Column(name = "title")
    private String title;

    @Column(name = "website")
    private String website;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "links_categories",
            joinColumns = @JoinColumn(name = "link_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;

    @Column(name = "description")
    private String description;

    @Column(name = "published")
    private boolean published;

}
