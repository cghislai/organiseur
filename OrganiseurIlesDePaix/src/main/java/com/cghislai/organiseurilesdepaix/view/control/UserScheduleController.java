/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.CampaignEvent;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.CampaignEventService;
import com.cghislai.organiseurilesdepaix.service.search.CampaignEventSearch;
import com.cghislai.organiseurilesdepaix.util.DateUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.DefaultScheduleEvent;
import org.primefaces.model.LazyScheduleModel;
import org.primefaces.model.ScheduleModel;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class UserScheduleController implements Serializable {

    @Inject
    private AuthController authController;
    @Inject
    private GlobalPreferencesController globalPreferencesController;
    @EJB
    private CampaignEventService campaignEventService;
    @EJB
    private CampaignDatesService campaignDatesService;

    private List<CampaignDay> campaignDays;
    // Schedule models per days
    private ScheduleModel[] models;

    @PostConstruct
    public void init() {
        campaignDays = campaignDatesService.findAllDays();

        models = new ScheduleModel[campaignDays.size()];
    }

    public ScheduleModel getScheduleModel(CampaignDay campaignDay) {
        int campaignDayIndex = campaignDays.indexOf(campaignDay);
        ScheduleModel model = models[campaignDayIndex];
        if (model == null) {
            model = new MyScheduleModel(campaignDay);
            models[campaignDayIndex] = model;
        }
        return model;
    }

    public List<CampaignDay> getCampaignDays() {
        return campaignDays;
    }

    private class MyScheduleModel extends LazyScheduleModel {

        private final CampaignDay campaignDay;
        private CampaignEventSearch campaignEventSearch;

        public MyScheduleModel(CampaignDay campaignDay) {
            this.campaignDay = campaignDay;
            User authenticatedUser = authController.getAuthenticatedUser();
            campaignEventSearch = new CampaignEventSearch();
            campaignEventSearch.setCampaignDay(campaignDay);
            campaignEventSearch.setUser(authenticatedUser);
        }

        @Override
        public void loadEvents(Date start, Date end) {
            List<CampaignEvent> campaignEvents = campaignEventService.findCampaignEvents(campaignEventSearch);
            for (CampaignEvent event : campaignEvents) {
                MyScheduleEvent scheduleEvent = new MyScheduleEvent(event);
                addEvent(scheduleEvent);
            }
        }

    }

    private class MyScheduleEvent extends DefaultScheduleEvent {

        private final CampaignEvent event;

        public MyScheduleEvent(CampaignEvent campaignEvent) {
            this.event = campaignEvent;
            setData(campaignEvent);
            setTitle(campaignEvent.getLocation().getName());
            String description = campaignEvent.getLocation().getAddress();
            description += ", " + campaignEvent.getLocation().getPostalCode();
            description += " " + campaignEvent.getLocation().getCity();
            description += ", " + campaignEvent.getPersonAmount() + " personne(s)";
            setDescription(description);

            CampaignDay campaignDay = campaignEvent.getCampaignDay();
            Date campaignDate = campaignDay.getDate();
            Date startTime = campaignEvent.getStartTime();
            Date startDate = DateUtils.timeAndDay(startTime, campaignDate);
            setStartDate(startDate);
            Date endTime = campaignEvent.getEndTime();
            Date endDate = DateUtils.timeAndDay(endTime, campaignDate);
            setEndDate(endDate);
            setEditable(false);
        }

    }

}
