package com.avesta.mastercrawler.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Category> children = new ArrayList<>();

    @ManyToMany(mappedBy = "categories")
    private List<News> news;

    @Column(name = "status")
    private String status;

    public Category() {
    }

    public Category(Integer id, String name, Category parent, List<Category> children, List<News> news, String status) {
        this.id = id;
        this.name = name;
        this.parent = parent;
        this.children = children;
        this.news = news;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public List<Category> getChildren() {
        return children;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public List<News> getNews() {
        return news;
    }

    public void setNews(List<News> news) {
        this.news = news;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static List<Map<String, Object>> toMapList(List<Category> categories) {
        return categories.stream().map(category -> {
            Map<String, Object> categoryMap = new HashMap<>();

            categoryMap.put("id", category.getId() != null ? category.getId() : "N/A");
            categoryMap.put("name", category.getName() != null ? category.getName() : "Unnamed");
            categoryMap.put("parentId", category.getParent() != null ? category.getParent().getId() : null);

            List<Map<String, Object>> children = category.getChildren().stream().map(child -> {
                Map<String, Object> childMap = new HashMap<>();
                childMap.put("id", child.getId());
                childMap.put("name", child.getName());
                return childMap;
            }).toList();

            categoryMap.put("children", children);

            return categoryMap;
        }).toList();
    }



}
