package com.avesta.mastercrawler.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "about_us")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AboutUs extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "description", columnDefinition = "TEXT")
    @Lob
    private String description;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "fax_number")
    private String faxNumber;

    @Column(name = "field_of_activity")
    private String fieldOfActivity;

    @Column(name = "email")
    private String email;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "concessionaire")
    private String concessionaire;

    @Column(name = "ceo")
    private String ceo;

    @Column(name = "chief_editor")
    private String chiefEditor;
}
