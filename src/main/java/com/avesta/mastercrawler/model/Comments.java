package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "comments")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comments extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "email")
    private String email;

    @Column(name = "name")
    private String name;

    @Column(name = "content", columnDefinition = "TEXT")
    @Lob
    private String content;

    @Column(name = "posted_date")
    private Date postedDate;

    @Column(name = "status")
    private boolean status;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

}
