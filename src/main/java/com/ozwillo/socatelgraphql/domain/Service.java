package com.ozwillo.socatelgraphql.domain;

public class Service {

    private String identifier;

    private String title;

    private String description;

    private String language;

    private String webLink;

    private Creator creator;

    private Location location;

    public Service() {
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void mapper(String key, String value) {
        switch (key) {
            case "identifier":
                identifier = value;
                break;
            case "title":
                title = value;
                break;
            case "description":
                description = value;
                break;
            case "language":
                language = value;
                break;
            case "webLink":
                webLink = value;
                break;
        }
    }

}
