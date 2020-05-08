package com.example.pma.model;

public class Route {
    public final String id;
    public final String content;
    public final String details;

    public Route(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return content;
    }
}
