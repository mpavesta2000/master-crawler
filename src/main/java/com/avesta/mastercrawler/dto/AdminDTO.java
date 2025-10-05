package com.avesta.mastercrawler.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminDTO {

    private Integer id;
    private String email;
    private String name;
    private String password;
    private boolean active;
    private String phoneNumber;
    private String address;
    private String city;
    private String country;
}
