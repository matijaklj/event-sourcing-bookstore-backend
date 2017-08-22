package com.kumuluz.ee.samples.catalogue.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
/**
 * @author Matija Kljun
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Book implements CommandData {

    private UUID id = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private String title;
    private String author;
    private String description;
    private Long amount;

    public Book(UUID id, String title, String author, String description, Long amount) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.description = description;
        this.amount = amount;
    }

    public Book(String title, String author, String description) {
        this.title = title;
        this.author = author;
        this.description = description;
    }

    public Book(Map<Keyword, Object> params) {
        this((UUID) params.get(new Keyword("id")),
            (String) params.get(new Keyword("title")),
            (String) params.get(new Keyword("author")),
            (String) params.get(new Keyword("description")),
            (Long) params.get(new Keyword("amount")));
    }

    @Override
    public Map<Keyword, Object> toMap() {
        Map<Keyword, Object> bookMap = new HashMap<Keyword, Object>();

        bookMap.put(new Keyword("id"), this.getId());
        bookMap.put(new Keyword("title"), this.getTitle());
        bookMap.put(new Keyword("author"), this.getAuthor());
        bookMap.put(new Keyword("description"), this.getDescription());
        bookMap.put(new Keyword("amount"), this.getAmount());

        return bookMap;
    }

    public Book() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }
}