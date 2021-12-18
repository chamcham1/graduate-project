package com.example.aafw.entity;

public class News {

    public String title;                // 제목
    public String link;                 // 네이버 하이퍼텍스트 link
    public String date;                 // 네이버에 제공된 시간

    public News(String title, String link, String date) {
        this.title = title;
        this.link = link;
        this.date = date;
    }
}
