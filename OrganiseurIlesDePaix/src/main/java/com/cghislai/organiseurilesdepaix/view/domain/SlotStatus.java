/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.domain;

import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot;
import com.cghislai.organiseurilesdepaix.domain.Subscription;
import java.util.List;

/**
 *
 * @author cghislai
 */
public class SlotStatus {

    private LocationTimeSlot timeSlot;
    private Long subscriptionAmount;
    private String label;
    private String tooltip;
    private boolean userSubscribed;
    private boolean editable;
    private List<Subscription> subscriptions;

    public LocationTimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(LocationTimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Long getSubscriptionAmount() {
        return subscriptionAmount;
    }

    public void setSubscriptionAmount(Long subscriptionAmount) {
        this.subscriptionAmount = subscriptionAmount;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public boolean isUserSubscribed() {
        return userSubscribed;
    }

    public void setUserSubscribed(boolean userSubscribed) {
        this.userSubscribed = userSubscribed;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }

}
