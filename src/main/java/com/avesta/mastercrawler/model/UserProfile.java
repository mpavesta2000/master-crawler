package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@ToString(exclude = "userId")
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {

    @Id
    @Column(name = "user_account_id")
    private Integer userAccountId;

    @OneToOne
    @JoinColumn(name = "user_account_id")
    @MapsId
    private Users userId;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "address")
    private String address;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "profile_photo", nullable = true)
    private String profilePhoto;

    @Column(name = "phone")
    private String phone;

    @Column(name = "email")
    private  String email;




}
