/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service.search;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import java.util.List;

/**
 *
 * @author cghislai
 */
public class SolverInfo {

    private List<Location> locations;
    private List<CampaignDay> campaignDay;

    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public List<CampaignDay> getCampaignDays() {
        return campaignDay;
    }

    public void setCampaignDay(List<CampaignDay> campaignDay) {
        this.campaignDay = campaignDay;
    }

}
