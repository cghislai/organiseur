/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author cghislai
 */
@Entity
@Table(name="availability")
public class Availabilitiy implements Serializable{

    @Id
    @GeneratedValue
    private Long id;
    @JoinColumn(nullable = false)
    @ManyToOne
    private User user;
    @JoinColumn(nullable = false)
    @ManyToOne
    private Location location;
    @JoinColumn(nullable = false)
    @ManyToOne
    private CampaignDay campaignDay;
    @Column(nullable = false)
    @Temporal( TemporalType.TIME)
    private Date startTime;
    @Column(nullable = false)
    @Temporal( TemporalType.TIME)
    private Date endTime;
    @Column(nullable = false)
    private Integer personAmount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public Integer getPersonAmount() {
        return personAmount;
    }

    public void setPersonAmount(Integer personAmount) {
        this.personAmount = personAmount;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Availabilitiy other = (Availabilitiy) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
