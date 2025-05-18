package com.example.notesapp;

public class Note {
    private int id;
    private String title;
    private String content;
    private int folderId; // foreign key

    // constructor, getters, and setters...
    public Note(int id, String title, String content, int folderId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.folderId = folderId;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public int getFolderId() {
        return folderId;
    }

    // Setters
    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setFolderId(int folderId) {
        this.folderId = folderId;
    }
}


