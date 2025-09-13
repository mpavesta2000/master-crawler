package com.avesta.mastercrawler.dto;

public class ScrapeRequest {

    private String url;


    public ScrapeRequest(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "ScrapeRequest{" +
                "url='" + url + '\'' +
                '}';
    }
}
