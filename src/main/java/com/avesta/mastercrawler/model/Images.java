package com.avesta.mastercrawler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "images")
public class Images {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "image_name")
    private String imageName;

    @Column(name = "image_src")
    private String imageSrc;

    public Images() {
    }

    public Images(String imageName, String imageSrc) {
        this.imageName = imageName;
        this.imageSrc = imageSrc;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    @Override
    public String toString() {
        return "Images{" +
                "id=" + id +
                ", imageName='" + imageName + '\'' +
                ", imageSrc='" + imageSrc + '\'' +
                '}';
    }
}
