package com.ozwillo.socatelgraphql.domain;

public class Owner {

    private String Identifier;

    private String Title;

    private String Description;

    private String WebLink;

    private String Language;

    private Integer NumLikes;

    public Owner() {
    }

    public String getIdentifier() {
        return Identifier;
    }

    public void setIdentifier(String identifier) {
        Identifier = identifier;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getWebLink() {
        return WebLink;
    }

    public void setWebLink(String webLink) {
        WebLink = webLink;
    }

    public String getLanguage() {
        return Language;
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public Integer getNumLikes() {
        return NumLikes;
    }

    public void setNumLikes(Integer numLikes) {
        NumLikes = numLikes;
    }
}
