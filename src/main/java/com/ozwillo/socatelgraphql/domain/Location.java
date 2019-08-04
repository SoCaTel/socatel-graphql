package com.ozwillo.socatelgraphql.domain;

public class Location {

    private String name;

    private String alternateName;

    private String countryCode;

    public Location() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlternateName() {
        return alternateName;
    }

    public void setAlternateName(String alternateName) {
        this.alternateName = alternateName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void mapper(String key, String value) {
        switch (key) {
            case "location_name":
                name = value;
                break;
            case "location_alternateName":
                alternateName = value;
                break;
            case "location_countryCode":
                countryCode = value;
                break;
        }
    }
}
