/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain.util;

import java.math.BigDecimal;

/**
 *
 * @author cghislai
 */
public class LocationCoordinateInfo {

    private Long locationId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String name;

    public LocationCoordinateInfo(Long locationId, BigDecimal latitude, BigDecimal longitude, String name) {
        this.locationId = locationId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public Long getLocationId() {
        return locationId;
    }

    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
