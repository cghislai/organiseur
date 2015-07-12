/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service.search;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author cghislai
 */
public class CampaignEventSearch implements Serializable {

    private User user;
    private Location location;
    private CampaignDay campaignDay;
    private Date forTime;
    private Date startTime;
    private Date endTime;
    private Pagination pagination;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

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

    public Date getForTime() {
        return forTime;
    }

    public void setForTime(Date forTime) {
        this.forTime = forTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

}
