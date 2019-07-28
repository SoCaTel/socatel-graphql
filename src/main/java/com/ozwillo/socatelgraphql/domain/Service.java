package com.ozwillo.socatelgraphql.domain;

import com.google.gson.annotations.SerializedName;

public class Service {

    @SerializedName("organisation_id")
    private String organisationId;
    @SerializedName("organisation_name")
    private String organisationName;
    @SerializedName("twitter_screen_name")
    private String twitterScreenName;
    @SerializedName("twitter_user_id")
    private String twitterUserId;

    public Service() {
    }

    public Service(String organisationId, String organisationName, String twitterScreenName, String twitterUserId) {
        this.organisationId = organisationId;
        this.organisationName = organisationName;
        this.twitterScreenName = twitterScreenName;
        this.twitterUserId = twitterUserId;
    }

    public String getOrganisationId() {
        return organisationId;
    }

    public String getOrganisationName() {
        return organisationName;
    }

    public String getTwitterScreenName() {
        return twitterScreenName;
    }

    public String getTwitterUserId() {
        return twitterUserId;
    }
}
