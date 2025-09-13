package com.avesta.mastercrawler.model;

import jakarta.persistence.*;

@Entity
@Table(name = "videos")
public class Videos extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "video_name")
    private String videoName;

    @Column(name = "video_src")
    private String videoSrc;

    @Column(name = "video_image")
    private String videoImage;

    public Videos() {
    }

    public Videos(String videoName, String videoSrc, String videoImage) {
        this.videoName = videoName;
        this.videoSrc = videoSrc;
        this.videoImage = videoImage;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoSrc() {
        return videoSrc;
    }

    public void setVideoSrc(String videoSrc) {
        this.videoSrc = videoSrc;
    }

    public String getVideoImage() {
        return videoImage;
    }

    public void setVideoImage(String videoImage) {
        this.videoImage = videoImage;
    }

    @Override
    public String toString() {
        return "Videos{" +
                "id=" + id +
                ", videoName='" + videoName + '\'' +
                ", videoSrc='" + videoSrc + '\'' +
                ", videoImage='" + videoImage + '\'' +
                '}';
    }
}
