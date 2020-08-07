package com.cmexpertise.customtagslibrary.models;

public class TagModel {

    private String name;

    public TagModel() {
    }

    public TagModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
