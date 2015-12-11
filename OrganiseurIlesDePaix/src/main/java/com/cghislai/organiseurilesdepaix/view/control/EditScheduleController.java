/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot;
import com.cghislai.organiseurilesdepaix.domain.Subscription;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.LocationTimeSlotService;
import com.cghislai.organiseurilesdepaix.service.SubscriptionService;
import com.cghislai.organiseurilesdepaix.service.UserService;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.service.search.LocationTimeSlotSearch;
import com.cghislai.organiseurilesdepaix.service.search.SubscriptionSearch;
import com.cghislai.organiseurilesdepaix.view.domain.LocationDayWithSlotsStatus;
import com.cghislai.organiseurilesdepaix.view.domain.SlotStatus;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.FacesException;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class EditScheduleController implements Serializable {

    @EJB
    private LocationService locationService;
    @EJB
    private LocationTimeSlotService locationTimeSlotService;
    @EJB
    private CampaignDatesService campaignDateService;
    @EJB
    private SubscriptionService subscriptionService;
    @EJB
    private UserService userService;
    @Inject
    private TimeSlotController timeSlotController;
    @Inject
    private AuthController authController;

    private CampaignDay campaignDay;
    private List<CampaignDay> allCampaignDays;
    private LazyDataModel<LocationDayWithSlotsStatus> locationsModel;

    private LocationTimeSlot editingTimeSlot;

    @PostConstruct
    public void init() {
        searchCampaignDays();
        searchLocationsSlots();
    }

    private void searchCampaignDays() {
        allCampaignDays = campaignDateService.findAllDays();
        if (campaignDay == null && !allCampaignDays.isEmpty()) {
            campaignDay = allCampaignDays.get(0);
        }
    }

    public void searchLocationsSlots() {
        if (campaignDay == null) {
            locationsModel = null;
            return;
        }
        locationsModel = new LocationsModel();
    }

    public void actionEditForSlot(LocationTimeSlot timeSlot) {
        this.editingTimeSlot = timeSlot;
    }

    public void actionCreateUser(String name) {
        User user = new User();
        user.setHumanName(name);
        user.setUserName(name);
        User savedUser = userService.saveUser(user);
        actionAddUser(savedUser);
    }

    public void actionAddUser(User user) {
        Subscription subscription = new Subscription();
        subscription.setLocationTimeSlot(editingTimeSlot);
        subscription.setUser(user);
        subscriptionService.saveSubscription(subscription);
        editingTimeSlot = null;
        searchLocationsSlots();
    }

    public void actionRemoveSubscription(Subscription subscription) {
        subscriptionService.deleteSubscription(subscription);
        searchLocationsSlots();
    }

    public CampaignDay getCampaignDay() {
        return campaignDay;
    }

    public void setCampaignDay(CampaignDay campaignDay) {
        this.campaignDay = campaignDay;
    }

    public List<CampaignDay> getAllCampaignDays() {
        return allCampaignDays;
    }

    public LazyDataModel<LocationDayWithSlotsStatus> getLocationsModel() {
        return locationsModel;
    }

    public LocationTimeSlot getEditingTimeSlot() {
        return editingTimeSlot;
    }

    private class LocationsModel extends LazyDataModel<LocationDayWithSlotsStatus> {

        @Override
        public List<LocationDayWithSlotsStatus> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
            return loadData(first, pageSize);
        }

        @Override
        public List<LocationDayWithSlotsStatus> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
            return loadData(first, pageSize);
        }

        private List<LocationDayWithSlotsStatus> loadData(int first, int pageSize) {
            LocationSearch locationSearch = new LocationSearch();
            Pagination pagination = new Pagination(first, pageSize);
            locationSearch.setPagination(pagination);

            Long locationCount = locationService.countLocation(locationSearch);
            setRowCount(locationCount.intValue());

            List<Location> locaitons = locationService.findLocations(locationSearch);

            List<LocationDayWithSlotsStatus> locationsList = locaitons.stream()
                    .map(this::createLocationWithSlotStatus)
                    .collect(Collectors.toList());
            return locationsList;
        }

        private LocationDayWithSlotsStatus createLocationWithSlotStatus(Location location) {
            Map<Long, SlotStatus> slotsMap = new HashMap<>();
            List<Long> timeSlots = timeSlotController.getAllSlots();

            timeSlots.stream()
                    .forEach((Long time) -> {
                        LocationTimeSlotSearch locationTimeSlotSearch = new LocationTimeSlotSearch();
                        locationTimeSlotSearch.setCampaignDay(campaignDay);
                        locationTimeSlotSearch.setLocation(location);
                        locationTimeSlotSearch.setStartHour(time);
                        List<LocationTimeSlot> locationsSlots = locationTimeSlotService.searchLocationTimeSlots(locationTimeSlotSearch, null);
                        if (locationsSlots.size() > 1) {
                            throw new FacesException("Multiple location slot");
                        }
                        if (locationsSlots.isEmpty()) {
                            slotsMap.put(time, null);
                            return;
                        }
                        LocationTimeSlot locationSlot = locationsSlots.get(0);
                        SubscriptionSearch subscriptionSearch = new SubscriptionSearch();
                        subscriptionSearch.setCampaignDay(campaignDay);
                        subscriptionSearch.setLocation(location);
                        subscriptionSearch.setStartHour(time);
                        List<Subscription> subscriptions = subscriptionService.searchSubscriptions(subscriptionSearch, null);

                        SlotStatus slotStatus = new SlotStatus();
                        slotStatus.setSubscriptionAmount(Long.valueOf(subscriptions.size()));
                        slotStatus.setTimeSlot(locationSlot);
                        slotStatus.setSubscriptions(subscriptions);
                        slotsMap.put(time, slotStatus);
                    });

            LocationDayWithSlotsStatus locationWithSlots = new LocationDayWithSlotsStatus();
            locationWithSlots.setCampaignDay(campaignDay);
            locationWithSlots.setLocation(location);
            locationWithSlots.setSlots(slotsMap);
            return locationWithSlots;
        }
    }
}
