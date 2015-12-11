/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.LocationTimeSlotService;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.service.search.LocationTimeSlotSearch;
import com.cghislai.organiseurilesdepaix.view.domain.LocationWithSlots;
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
public class LocationSlotsController implements Serializable {

    @EJB
    private LocationService locationService;
    @EJB
    private LocationTimeSlotService locationTimeSlotService;
    @EJB
    private CampaignDatesService campaignDateService;
    @Inject
    private TimeSlotController timeSlotController;

    private CampaignDay campaignDay;
    private List<CampaignDay> allCampaignDays;
    private LazyDataModel<LocationWithSlots> locationsModel;

    public LocationSlotsController() {
    }

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

    public void onLocationSlotChanged(Location location, long time, boolean value) {
        LocationTimeSlotSearch timeSlotSearch = new LocationTimeSlotSearch();
        timeSlotSearch.setCampaignDay(campaignDay);
        timeSlotSearch.setLocation(location);
        timeSlotSearch.setStartHour(time);
        List<LocationTimeSlot> slots = locationTimeSlotService.searchLocationTimeSlots(timeSlotSearch, null);
        if (value) {
            if (!slots.isEmpty()) {
                throw new FacesException("Slot already exists");
            }
            LocationTimeSlot locationTimeSlot = new LocationTimeSlot();
            locationTimeSlot.setCampaignDay(campaignDay);
            locationTimeSlot.setLocation(location);
            locationTimeSlot.setStartTime(time);
            LocationTimeSlot savedSlot = locationTimeSlotService.saveLocationTimeSlot(locationTimeSlot);
        } else {
            if (slots.size() != 1) {
                throw new FacesException("Slot does not exist");
            }
            LocationTimeSlot slot = slots.get(0);
            locationTimeSlotService.deleteLocationTimeSlot(slot);
        }
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

    public LazyDataModel<LocationWithSlots> getLocationsModel() {
        return locationsModel;
    }

    private class LocationsModel extends LazyDataModel<LocationWithSlots> {

        @Override
        public List<LocationWithSlots> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
            return loadData(first, pageSize);
        }

        @Override
        public List<LocationWithSlots> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
            return loadData(first, pageSize);
        }

        private List<LocationWithSlots> loadData(int first, int pageSize) {
            LocationSearch locationSearch = new LocationSearch();
            Pagination pagination = new Pagination(first, pageSize);
            locationSearch.setPagination(pagination);

            Long locationCount = locationService.countLocation(locationSearch);
            setRowCount(locationCount.intValue());

            List<Location> locaitons = locationService.findLocations(locationSearch);

            List<LocationWithSlots> locationsList = locaitons.stream()
                    .map(this::createLocationWithSlots)
                    .collect(Collectors.toList());
            return locationsList;
        }

        private LocationWithSlots createLocationWithSlots(Location location) {
            Map<Long, LocationTimeSlot> slotsMap = new HashMap<>();
            List<Long> timeSlots = timeSlotController.getAllSlots();
            timeSlots.stream()
                    .forEach((time) -> {
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
                        } else {
                            slotsMap.put(time, locationsSlots.get(0));
                        }
                    });

            LocationWithSlots locationWithSlots = new LocationWithSlots();
            locationWithSlots.setCampaignDay(campaignDay);
            locationWithSlots.setLocation(location);
            locationWithSlots.setSlots(slotsMap);
            return locationWithSlots;
        }
    }
}
