package com.ozwillo.socatelgraphql.domain;

public class Owner {

    private String identifier;

    private String title;

    private String description;

    private String webLink;

    private String language;

    private Integer numLikes;

    public Owner() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebLink() {
        return webLink;
    }

    public void setWebLink(String webLink) {
        this.webLink = webLink;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getNumLikes() {
        return numLikes;
    }

    public void setNumLikes(Integer numLikes) {
        this.numLikes = numLikes;
    }

    public void mapper(String key, String value) {
        switch (key) {
            case "owner_identifier":
                identifier = value;
                break;
            case "owner_title":
                title = value;
                break;
            case "owner_description":
                description = value;
                break;
            case "owner_webLink":
                webLink = value;
                break;
            case "owner_language":
                language = value;
                break;
            case "owner_numLikes":
                numLikes = Integer.valueOf(value);
                break;
        }
    }
}
