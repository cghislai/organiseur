/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service.search;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import java.util.List;
import java.util.Map;

/**
 *
 * @author cghislai
 */
public class SolverUserInfo {

    private User user;
    private CampaignDay campaignDay;
    private Map<Location, List<Availability>> availabilitiesPerLocation;
    private Map<Location, Integer> personAmountPerLocation;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CampaignDay getCampaignDay() {
        return campaignDay;
    }

    public void setCampaignDay(CampaignDay campaignDay) {
        this.campaignDay = campaignDay;
    }

    public Map<Location, List<Availability>> getAvailabilitiesPerLocation() {
        return availabilitiesPerLocation;
    }

    public void setAvailabilitiesPerLocation(Map<Location, List<Availability>> availabilitiesPerLocation) {
        this.availabilitiesPerLocation = availabilitiesPerLocation;
    }

    public Map<Location, Integer> getPersonAmountPerLocation() {
        return personAmountPerLocation;
    }

    public void setPersonAmountPerLocation(Map<Location, Integer> personAmountPerLocation) {
        this.personAmountPerLocation = personAmountPerLocation;
    }

}
