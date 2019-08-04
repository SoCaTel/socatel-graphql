package com.ozwillo.socatelgraphql.domain;

import java.time.ZonedDateTime;

public class Post {

    private String identifier;

    private String description;

    private ZonedDateTime creationDate;

    private String language;

    private Integer numLikes;

    private Integer numReplies;

    private Location location;

    private Owner owner;

    private Creator creator;

    public Post() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
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

    public Integer getNumReplies() {
        return numReplies;
    }

    public void setNumReplies(Integer numReplies) {
        this.numReplies = numReplies;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public void mapper(String key, String value) {
        switch (key) {
            case "identifier":
                identifier = value;
                break;
            case "description":
                description = value;
                break;
            case "creationDate":
                creationDate = ZonedDateTime.parse(value);
                break;
            case "language":
                language = value;
                break;
            case "num_likes":
                numLikes = Integer.valueOf(value);
                break;
            case "num_replies":
                numReplies = Integer.valueOf(value);
                break;
        }
    }
}
