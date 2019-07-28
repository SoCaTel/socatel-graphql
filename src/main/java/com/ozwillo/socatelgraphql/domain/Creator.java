package com.ozwillo.socatelgraphql.domain;

public class Creator {

    private String name;

    private String username;

    public Creator() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void mapper(String key, String value) {
        switch (key) {
            case "creator_name":
                name = value;
                break;
            case "creator_username":
                username = value;
                break;
        }
    }
}
