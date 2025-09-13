package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users_type")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UsersType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_type_id")
    private Integer userTypeId;

    @Column(name = "user_type_name")
    private String userTypeName;

    @OneToMany(mappedBy = "userTypeId",cascade = CascadeType.ALL)
    private List<Users> users;

}
