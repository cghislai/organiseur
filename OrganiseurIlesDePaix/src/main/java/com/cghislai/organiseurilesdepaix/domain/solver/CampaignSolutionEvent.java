/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain.solver;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import java.util.List;
import java.util.stream.Collectors;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

/**
 *
 * @author cghislai
 */
@PlanningEntity
public class CampaignSolutionEvent {

    private User user;
    @PlanningVariable(valueRangeProviderRefs = "locationsRange", nullable = true)
    private Location location;
    private TimeSlot timeSlot;
    private List<Availability> availabilities;

    public void init(User user, List<Availability> availabilities) {
        this.user = user;
        this.availabilities = availabilities;
    }

    public int getPersonAmount() {
        if (location == null) {
            return 0;
        }
        int amout = availabilities.stream()
                .filter(a -> a.getLocation() == location)
                .filter(a -> TimeSlot.availabilityMatches(timeSlot, a))
                .mapToInt(a -> a.getPersonAmount())
                .max()
                .orElse(0);
        return amout;
    }

    @ValueRangeProvider(id = "locationsRange")
    public List<Location> getAvailableLocations() {
        return availabilities.stream()
                .filter(a -> TimeSlot.availabilityMatches(timeSlot, a))
                .map(a -> a.getLocation())
                .collect(Collectors.toList());
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

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public List<Availability> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<Availability> availabilities) {
        this.availabilities = availabilities;
    }

}
