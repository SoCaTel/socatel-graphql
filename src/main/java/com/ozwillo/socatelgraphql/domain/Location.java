package com.ozwillo.socatelgraphql.domain;

public class Location {

    private String Name;

    private String AlternateName;

    private String CountryCode;

    public Location() {
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAlternateName() {
        return AlternateName;
    }

    public void setAlternateName(String alternateName) {
        AlternateName = alternateName;
    }

    public String getCountryCode() {
        return CountryCode;
    }

    public void setCountryCode(String countryCode) {
        CountryCode = countryCode;
    }
}
