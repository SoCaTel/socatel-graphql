package com.ozwillo.socatelgraphql.domain.DTO;

public class PostDTO {

    private String identifier;

    private String description;

    private String creationDate;

    private String language;

    private Integer numLikes;

    private Integer numReplies;

    private String locationName;

    private String locationAlternateName;

    private String locationCountryCode;

    private String ownerIdentifier;

    private String ownerTitle;

    private String ownerDescription;

    private String ownerWebLink;

    private String ownerLanguage;

    private Integer ownerNumLikes;

    private String creatorName;

    private String creatorUsername;


    public PostDTO() {
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

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
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

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationAlternateName() {
        return locationAlternateName;
    }

    public void setLocationAlternateName(String locationAlternateName) {
        this.locationAlternateName = locationAlternateName;
    }

    public String getLocationCountryCode() {
        return locationCountryCode;
    }

    public void setLocationCountryCode(String locationCountryCode) {
        this.locationCountryCode = locationCountryCode;
    }

    public String getOwnerIdentifier() {
        return ownerIdentifier;
    }

    public void setOwnerIdentifier(String ownerIdentifier) {
        this.ownerIdentifier = ownerIdentifier;
    }

    public String getOwnerTitle() {
        return ownerTitle;
    }

    public void setOwnerTitle(String ownerTitle) {
        this.ownerTitle = ownerTitle;
    }

    public String getOwnerDescription() {
        return ownerDescription;
    }

    public void setOwnerDescription(String ownerDescription) {
        this.ownerDescription = ownerDescription;
    }

    public String getOwnerWebLink() {
        return ownerWebLink;
    }

    public void setOwnerWebLink(String ownerWebLink) {
        this.ownerWebLink = ownerWebLink;
    }

    public String getOwnerLanguage() {
        return ownerLanguage;
    }

    public void setOwnerLanguage(String ownerLanguage) {
        this.ownerLanguage = ownerLanguage;
    }

    public Integer getOwnerNumLikes() {
        return ownerNumLikes;
    }

    public void setOwnerNumLikes(Integer ownerNumLikes) {
        this.ownerNumLikes = ownerNumLikes;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }
}
