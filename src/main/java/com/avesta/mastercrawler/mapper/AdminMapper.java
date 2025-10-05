package com.avesta.mastercrawler.mapper;

import com.avesta.mastercrawler.dto.AdminDTO;
import com.avesta.mastercrawler.model.Users;

public class AdminMapper {

    public static AdminDTO toDTO(Users user) {
        AdminDTO dto = new AdminDTO();

        dto.setId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getUserProfile().getFirstName() + " " + user.getUserProfile().getLastName());
        dto.setPassword(user.getPassword());
        dto.setActive(user.getActive());
        dto.setPhoneNumber(user.getUserProfile().getPhone());
        dto.setAddress(user.getUserProfile().getAddress());
        dto.setCity(user.getUserProfile().getCity());
        dto.setCountry(user.getUserProfile().getCountry());
        return dto;
    }
}
