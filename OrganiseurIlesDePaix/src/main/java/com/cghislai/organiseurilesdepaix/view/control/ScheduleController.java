/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.CampaignEvent;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.AvailabilityService;
import com.cghislai.organiseurilesdepaix.service.AvailabilitySolverService;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.CampaignEventService;
import com.cghislai.organiseurilesdepaix.service.GlobalPreferenceService;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.search.CampaignDaySearch;
import com.cghislai.organiseurilesdepaix.service.search.CampaignEventSearch;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.util.DateUtils;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.ScheduleEntryMoveEvent;
import org.primefaces.event.ScheduleEntryResizeEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class ScheduleController implements Serializable {

    @EJB
    private AvailabilitySolverService solverService;
    @EJB
    private LocationService locationService;
    @EJB
    private CampaignDatesService campaignDatesService;
    @EJB
    private AvailabilityService availabilityService;
    @EJB
    private CampaignEventService campaignEventService;
    @Inject
    private GlobalPreferencesController preferencesController;

    private long campaignEventsAmount;
    private long usersAmount;
    private long locationsAmount;
    private long campaignDayAmount;
    private boolean solverRunning = false;
    private boolean solverDone = false;
    private Future<List<CampaignEvent>> solverResult;
    //
    private List<CampaignDay> allCampaignDays;
    private List<Location> allLocations;
    private ScheduleModel[][] scheduleModels;
    private CampaignEvent editingEvent;

    @PostConstruct
    public void init() {
        searchAmounts();
        if (hasSchedule()) {
            loadSchedule();
        }
    }

    public void actionCloseRegistrations() {
        preferencesController.setPreferenceBoolean(GlobalPreferenceService.KEY_REGISTRATION_CLOSED, Boolean.TRUE);
    }

    public void actionGenerate() {
        solverRunning = true;
        solverResult = solverService.resolve();
    }

    public void actionCheckDone() {
        if (solverResult == null) {
            return;
        }
        if (solverResult.isDone()) {
            solverDone = true;

            onSolverDone();
        }
    }

    private void onSolverDone() {
        try {
            // Save campaign events
            List<CampaignEvent> events = solverResult.get();
            events.stream().forEach((event) -> {
                campaignEventService.saveCampaignEvent(event);
            });
            loadSchedule();
        } catch (InterruptedException | ExecutionException ex) {
            // TODO: add message
        }
    }

    private void loadSchedule() {
        searchAmounts();
        allCampaignDays = campaignDatesService.findAllDays();
        allLocations = locationService.findLocations(new LocationSearch());
        searchSchedules();
    }

    public void actionStopSolver() {
        solverRunning = false;
    }

    public void actionPublishSchedule() {
        preferencesController.setPreferenceBoolean(GlobalPreferenceService.KEY_SCHEDULE_ONLINE, Boolean.TRUE);
    }

    public void actionUnPublishSchedule() {
        preferencesController.removeGlobalPreference(GlobalPreferenceService.KEY_SCHEDULE_ONLINE);
    }

    public void actionDeleteSchedule() {
        actionUnPublishSchedule();
        campaignEventService.deleteAllCampaignEvents();
        searchAmounts();
    }

    public void actionSaveEvent() {
        campaignEventService.saveCampaignEvent(editingEvent);
        editingEvent = null;
        loadSchedule();
    }

    public void onEventSelect(SelectEvent selectEvent) {
        CampaignScheduleEvent event = (CampaignScheduleEvent) selectEvent.getObject();
        editingEvent = event.getEvent();
    }

    public void onEventMove(ScheduleEntryMoveEvent event) {
        int minuteDelta = event.getMinuteDelta();
        CampaignScheduleEvent scheduleEvent = (CampaignScheduleEvent) event.getScheduleEvent();
        CampaignEvent campaignEvent = scheduleEvent.getEvent();
        Date startTime = campaignEvent.getStartTime();
        Date newStartTime = DateUtils.addMinutes(startTime, minuteDelta);
        campaignEvent.setStartTime(newStartTime);
        Date endTime = campaignEvent.getEndTime();
        Date newEndTime = DateUtils.addMinutes(endTime, minuteDelta);
        campaignEvent.setEndTime(newEndTime);

    }

    public void onEventResize(ScheduleEntryResizeEvent event) {
        int minuteDelta = event.getMinuteDelta();
        CampaignScheduleEvent scheduleEvent = (CampaignScheduleEvent) event.getScheduleEvent();
        CampaignEvent campaignEvent = scheduleEvent.getEvent();
        Date endTime = campaignEvent.getEndTime();
        Date newEndTime = DateUtils.addMinutes(endTime, minuteDelta);
        campaignEvent.setEndTime(newEndTime);

        CampaignEvent newEvent = campaignEventService.saveCampaignEvent(campaignEvent);
        scheduleEvent.setEvent(newEvent);
    }

    private void searchAmounts() {
        usersAmount = availabilityService.countUsersWithAvailabilites();
        locationsAmount = locationService.countLocation(new LocationSearch());
        campaignDayAmount = campaignDatesService.countCampaignDays(new CampaignDaySearch());
        campaignEventsAmount = campaignEventService.countCampaignEvents(new CampaignEventSearch());
    }

    private void searchSchedules() {
        scheduleModels = new MyScheduleModel[(int) locationsAmount][(int) campaignDayAmount];
        for (int locationIndex = 0; locationIndex < locationsAmount; locationIndex++) {
            Location location = allLocations.get(locationIndex);
            for (int campaignDayIndex = 0; campaignDayIndex < campaignDayAmount; campaignDayIndex++) {
                CampaignDay campaignDay = allCampaignDays.get(campaignDayIndex);
                MyScheduleModel model = new MyScheduleModel(location, campaignDay);
                scheduleModels[locationIndex][campaignDayIndex] = model;
            }
        }
    }

    public boolean hasSchedule() {
        Boolean scheduleOnline = preferencesController.getScheduleOnline();
        return Objects.equals(scheduleOnline, Boolean.TRUE)
                || campaignEventsAmount > 0;
    }

    public String getCampaignDayLabel(CampaignDay campaignDay) {
        DateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy");
        dateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Brussels"));
        String format = dateFormat.format(campaignDay.getDate());
        return format;
    }

    public long getUsersAmount() {
        return usersAmount;
    }

    public long getLocationsAmount() {
        return locationsAmount;
    }

    public long getCampaignDayAmount() {
        return campaignDayAmount;
    }

    public boolean isSolverRunning() {
        return solverRunning;
    }

    public boolean isSolverDone() {
        return solverDone;
    }

    public List<CampaignDay> getAllCampaignDays() {
        return allCampaignDays;
    }

    public List<Location> getAllLocations() {
        return allLocations;
    }

    public CampaignEvent getEditingEvent() {
        return editingEvent;
    }

    public ScheduleModel getScheduleModel(Location location, CampaignDay campaignDay) {
        int locationIndex = allLocations.indexOf(location);
        int campaignDayIndex = allCampaignDays.indexOf(campaignDay);
        return scheduleModels[locationIndex][campaignDayIndex];
    }

    private class MyScheduleModel extends LazyScheduleModel {

        private final CampaignEventSearch eventSearch;

        public MyScheduleModel(Location location, CampaignDay campaignDay) {
            eventSearch = new CampaignEventSearch();
            eventSearch.setCampaignDay(campaignDay);
            eventSearch.setLocation(location);
        }

        @Override
        public void loadEvents(Date start, Date end) {
            List<CampaignEvent> campaignEvents = campaignEventService.findCampaignEvents(eventSearch);
            for (CampaignEvent event : campaignEvents) {
                CampaignScheduleEvent scheduleEvent = new CampaignScheduleEvent(event);
                addEvent(scheduleEvent);
            }
        }

    }

    private class CampaignScheduleEvent extends DefaultScheduleEvent {

        private CampaignEvent event;

        public CampaignScheduleEvent(CampaignEvent event) {
            this.event = event;
            setData(event);
            User user = event.getUser();
            Integer personAmount = event.getPersonAmount();
            String title = user.getHumanName() + " ("
                    + personAmount + " personnes)";
            setTitle(title);
            CampaignDay campaignDay = event.getCampaignDay();
            Date campaignDate = campaignDay.getDate();
            Date startTime = event.getStartTime();
            Date startDate = DateUtils.timeAndDay(startTime, campaignDate);
            setStartDate(startDate);
            Date endTime = event.getEndTime();
            Date endDate = DateUtils.timeAndDay(endTime, campaignDate);
            setEndDate(endDate);
            setEditable(true);
        }

        public CampaignEvent getEvent() {
            return event;
        }

        public void setEvent(CampaignEvent event) {
            this.event = event;
        }

    }
}
