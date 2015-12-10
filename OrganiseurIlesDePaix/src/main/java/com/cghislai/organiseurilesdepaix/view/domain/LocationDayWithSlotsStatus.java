/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.domain;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author cghislai
 */
public class LocationDayWithSlotsStatus implements Serializable {

    private Location location;
    private CampaignDay campaignDay;
    private Map<Long, SlotStatus> slots;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public CampaignDay getCampaignDay() {
        return campaignDay;
    }

    public void setCampaignDay(CampaignDay campaignDay) {
        this.campaignDay = campaignDay;
    }

    public Map<Long, SlotStatus> getSlots() {
        return slots;
    }

    public void setSlots(Map<Long, SlotStatus> slots) {
        this.slots = slots;
    }

}
