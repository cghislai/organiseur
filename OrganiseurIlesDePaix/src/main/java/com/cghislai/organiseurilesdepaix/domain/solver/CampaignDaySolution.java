/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.domain.solver;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

/**
 *
 * @author cghislai
 */
@PlanningSolution
public class CampaignDaySolution implements Solution<HardMediumSoftScore> {

    private List<Availability> availabilities;
    private List<User> users;
    @ValueRangeProvider(id = "timeSlotRange")
    private List<TimeSlot> timeSlots;

    @PlanningEntityCollectionProperty
    private List<CampaignSolutionEvent> events;
    private HardMediumSoftScore score;
    private CampaignDay campaignDay;
    private List<Location> locations;

    public CampaignDaySolution() {
    }

    public void init(CampaignDay campaignDay, List<Availability> availabilities, List<Location> locations) {
        this.campaignDay = campaignDay;
        this.availabilities = availabilities;
        this.locations = locations;
        this.users = availabilities.stream()
                .map(a -> a.getUser())
                .distinct()
                .collect(Collectors.toList());
        timeSlots = new ArrayList<>();
        for (int hour = 7; hour < 20; hour++) {
            TimeSlot timeSlot = new TimeSlot();
            timeSlot.setHour(hour);
            timeSlots.add(timeSlot);
        }
        this.events = new ArrayList<>();
        for (User user : users) {
            for (TimeSlot timeSlot : timeSlots) {
                List<Availability> userAvailabilities = availabilities.stream()
                        .filter(a -> a.getUser() == user)
                        .filter(a -> TimeSlot.availabilityMatches(timeSlot, a))
                        .collect(Collectors.toList());
                CampaignSolutionEvent solutionEvent = new CampaignSolutionEvent();
                solutionEvent.setUser(user);
                solutionEvent.setTimeSlot(timeSlot);
                solutionEvent.setAvailabilities(userAvailabilities);
                events.add(solutionEvent);
            }
        }
    }

    @Override
    public HardMediumSoftScore getScore() {
        return score;
    }

    @Override
    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    @Override
    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.add(availabilities);
        return facts;
    }

    public List<CampaignSolutionEvent> getEvents() {
        return events;
    }

    public void setEvents(List<CampaignSolutionEvent> events) {
        this.events = events;
    }

    public List<Availability> getAvailabilities() {
        return availabilities;
    }

    public CampaignDay getCampaignDay() {
        return campaignDay;
    }

    public List<User> getUsers() {
        return users;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public List<Location> getLocations() {
        return locations;
    }

}
