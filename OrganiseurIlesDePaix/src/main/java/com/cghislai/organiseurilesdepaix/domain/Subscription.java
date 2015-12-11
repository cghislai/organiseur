/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 *
 * @author cghislai
 */
@Entity
@Table(name = "subscription")
public class Subscription implements Serializable, WithId {

    @Column(nullable = false)
    @Id
    @GeneratedValue
    private Long id;
    @JoinColumn(name = "USER_ID", nullable = false)
    @ManyToOne
    private User user;
    @JoinColumn(name = "LOCATION_SLOT_ID", nullable = false)
    @ManyToOne
    private LocationTimeSlot locationTimeSlot;

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

    public LocationTimeSlot getLocationTimeSlot() {
        return locationTimeSlot;
    }

    public void setLocationTimeSlot(LocationTimeSlot locationTimeSlot) {
        this.locationTimeSlot = locationTimeSlot;
    }

}
