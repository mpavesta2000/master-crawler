package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@ToString(exclude = "userProfile")
@AllArgsConstructor
@NoArgsConstructor
public class Users extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private int userId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "active")
    private Boolean Active;

    @OneToOne(mappedBy = "userId", cascade = CascadeType.ALL)
    private UserProfile userProfile;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_type_id")
    private UsersType userTypeId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserMonthlyReport> monthlyReports = new ArrayList<>();


    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<News> news = new ArrayList<>();

    @OneToMany(mappedBy = "userId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tags> tags = new ArrayList<>();

}
