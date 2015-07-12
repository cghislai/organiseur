/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.AvailabilityService;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.search.AvailabilitySearch;
import com.cghislai.organiseurilesdepaix.service.search.CampaignDaySearch;
import com.cghislai.organiseurilesdepaix.util.DateUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.DefaultScheduleModel;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleEvent;
import org.primefaces.model.ScheduleModel;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class AvailabilitiesController implements Serializable {

    @Inject
    private AuthController authController;
    @Inject
    private GlobalPreferencesController globalPreferencesController;
    @EJB
    private AvailabilityService availabilityService;
    @EJB
    private CampaignDatesService campaignDatesService;

    private List<CampaignDay> campaignDays;
    private List<Location> locations;
    // Schedule models location x days
    private ScheduleModel[][] models;
    private Availability editingAvailability;

    @PostConstruct
    public void init() {
        User authenticatedUser = authController.getAuthenticatedUser();
        AvailabilitySearch availabilitySearch = new AvailabilitySearch();
        availabilitySearch.setUser(authenticatedUser);
        campaignDays = availabilityService.findAvailabilitiesDays(availabilitySearch);
        locations = availabilityService.findAvailabilitiesLocations(availabilitySearch);

        models = new ScheduleModel[locations.size()][campaignDays.size()];
    }

    public ScheduleModel getScheduleModel(Location location, CampaignDay campaignDay) {
        int locationIndex = locations.indexOf(location);
        int campaignDayIndex = campaignDays.indexOf(campaignDay);
        ScheduleModel model = models[locationIndex][campaignDayIndex];
        if (model == null) {
            model = new MyScheduleModel(location, campaignDay);
            models[locationIndex][campaignDayIndex] = model;
        }
        return model;
    }

    public void onEventSelect(SelectEvent selectEvent) {
        MyScheduleEvent event = (MyScheduleEvent) selectEvent.getObject();
        editingAvailability = event.getAvailabilitiy();
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {
        int minuteDelta = event.getMinuteDelta();
        MyScheduleEvent scheduleEvent = (MyScheduleEvent) event.getScheduleEvent();
        Availability availabilitiy = scheduleEvent.getAvailabilitiy();
        Date startTime = availabilitiy.getStartTime();
        Date newStartTime = DateUtils.addMinutes(startTime, minuteDelta);
        availabilitiy.setStartTime(newStartTime);
        Date endTime = availabilitiy.getEndTime();
        Date newEndTime = DateUtils.addMinutes(endTime, minuteDelta);
        availabilitiy.setEndTime(newEndTime);

        Availability newAvailabilitiy = availabilityService.saveAvailability(availabilitiy);
        scheduleEvent.setAvailabilitiy(newAvailabilitiy);
    }

    public void onEventResize(ScheduleEntryResizeEvent event) {
        int minuteDelta = event.getMinuteDelta();
        MyScheduleEvent scheduleEvent = (MyScheduleEvent) event.getScheduleEvent();
        Availability availabilitiy = scheduleEvent.getAvailabilitiy();
        Date endTime = availabilitiy.getEndTime();
        Date newEndTime = DateUtils.addMinutes(endTime, minuteDelta);
        availabilitiy.setEndTime(newEndTime);

        Availability newAvailabilitiy = availabilityService.saveAvailability(availabilitiy);
        scheduleEvent.setAvailabilitiy(newAvailabilitiy);
    }
    
    public void actionNewEvent(CampaignDay campaignDay, Location location) {
        editingAvailability = new Availability();
        User authenticatedUser = authController.getAuthenticatedUser();
        editingAvailability.setCampaignDay(campaignDay);
        editingAvailability.setLocation(location);
        editingAvailability.setPersonAmount(1);
        editingAvailability.setUser(authenticatedUser);
    }
    
    public void deleteEvent() {
        availabilityService.removeAvailability(editingAvailability);
        editingAvailability = null;
    }
    public void saveEvent() {
        Availability savedAvailability = availabilityService.saveAvailability(editingAvailability);
        editingAvailability = null;
    }

    public List<CampaignDay> getCampaignDays() {
        return campaignDays;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public Availability getEditingAvailability() {
        return editingAvailability;
    }

    private class MyScheduleModel extends LazyScheduleModel {

        private final Location location;
        private final CampaignDay campaignDay;
        private AvailabilitySearch availabilitySearch;

        public MyScheduleModel(Location location, CampaignDay campaignDay) {
            this.location = location;
            this.campaignDay = campaignDay;
            User authenticatedUser = authController.getAuthenticatedUser();
            availabilitySearch = new AvailabilitySearch();
            availabilitySearch.setCampaignDay(campaignDay);
            availabilitySearch.setLocation(location);
            availabilitySearch.setUser(authenticatedUser);
        }

        @Override
        public void loadEvents(Date start, Date end) {
            List<Availability> availabilities = availabilityService.findAvailabilities(availabilitySearch);
            for (Availability availabilitiy : availabilities) {
                MyScheduleEvent event = new MyScheduleEvent(availabilitiy);
                addEvent(event);
            }
        }

    }

    private class MyScheduleEvent extends DefaultScheduleEvent {

        private Availability availabilitiy;

        public MyScheduleEvent(Availability availabilitiy) {
            this.availabilitiy = availabilitiy;
            setData(availabilitiy);
            setTitle(availabilitiy.getPersonAmount() + " personne(s)");
            CampaignDay campaignDay = availabilitiy.getCampaignDay();
            Date campaignDate = campaignDay.getDate();
            Date startTime = availabilitiy.getStartTime();
            Date startDate = DateUtils.timeAndDay(startTime, campaignDate);
            setStartDate(startDate);
            Date endTime = availabilitiy.getEndTime();
            Date endDate = DateUtils.timeAndDay(endTime, campaignDate);
            setEndDate(endDate);
            final Boolean registrationClosed = globalPreferencesController.getRegistrationClosed();
            setEditable(registrationClosed == null || registrationClosed == false);
        }

        public Availability getAvailabilitiy() {
            return availabilitiy;
        }

        public void setAvailabilitiy(Availability availabilitiy) {
            this.availabilitiy = availabilitiy;
        }

    }

}
