/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Availability;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.DayTimeAvailability;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.AvailabilityService;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.util.DateUtils;
import com.cghislai.organiseurilesdepaix.util.MyLazyDataModel;
import com.cghislai.organiseurilesdepaix.view.Views;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class RegisterAvailabilityController implements Serializable {

    @EJB
    private LocationService locationService;
    @EJB
    private CampaignDatesService campaignDatesService;
    @EJB
    private AvailabilityService availabilityService;
    @Inject
    private AuthController authController;
    @Inject
    private UserSessionController userSessionController;

    private Step currentStep;
    // Step 1
    private List<CampaignDay> allDates;
    private Map<CampaignDay, DayTimeAvailability> datesAvailabilities;
    private Integer personAmount;
    // Step 2
    private LocationSearch locationSearch;
    private List<Location> selectedLocations;
    private LazyDataModel<Location> locationsModel;
    private MapModel googleMapModel;

    public enum Step {

        SELECT_DATES,
        SELECT_LOCATIONS;
    }

    @PostConstruct
    public void init() {
        currentStep = Step.SELECT_DATES;
        datesAvailabilities = new HashMap<>();
        personAmount = 1;
        searchAllCampaignDates();
        // Step 2
        locationSearch = new LocationSearch();
        selectedLocations = new ArrayList<>();
        googleMapModel = new DefaultMapModel();
        searchLocations();
    }

    public void actionSwitchToStep(Step step) {
        currentStep = step;
    }

    public String actionFinish() {
        User authenticatedUser = authController.getAuthenticatedUser();
        for (DayTimeAvailability dayTimeAvailability : datesAvailabilities.values()) {
            for (Location location : selectedLocations) {
                Date startDate = dayTimeAvailability.getStartTime();
                Date endDate = dayTimeAvailability.getEndTime();
                startDate = DateUtils.timeOnly(startDate);
                endDate = DateUtils.timeOnly(endDate);
                Availability disponibility = new Availability();
                disponibility.setCampaignDay(dayTimeAvailability.getCampaignDay());
                disponibility.setEndTime(endDate);
                disponibility.setLocation(location);
                disponibility.setPersonAmount(personAmount);
                disponibility.setStartTime(startDate);
                disponibility.setUser(authenticatedUser);
                availabilityService.saveAvailability(disponibility);
            }
        }
        userSessionController.updateAvailabilities();
        return Views.USER_AVAILABILITIES;
    }

    public void actionSelectDate(CampaignDay campaignDay) {
        DayTimeAvailability availability = new DayTimeAvailability();
        availability.setCampaignDay(campaignDay);
        datesAvailabilities.put(campaignDay, availability);
    }

    public void actionUnselectDate(CampaignDay campaignDay) {
        datesAvailabilities.remove(campaignDay);
    }

    public boolean isDateSelected(CampaignDay campaignDay) {
        return datesAvailabilities.containsKey(campaignDay);
    }

    public DayTimeAvailability getTimeAvailability(CampaignDay campaignDay) {
        return datesAvailabilities.get(campaignDay);
    }

    public void actionSearchLocations() {
        searchLocations();
    }

    public void actionSelectLocation(Location location) {
        selectedLocations.add(location);
        updateMarkers();
    }

    public void actionUnselectLocation(Location location) {
        selectedLocations.remove(location);
        updateMarkers();
    }

    public boolean isLocationSelected(Location location) {
        return selectedLocations.contains(location);
    }

    public String getDateLabel(CampaignDay campaignDay) {
        DateFormat dateFormat = new SimpleDateFormat("EEEE dd/MM/yyyy");
        String formatted = dateFormat.format(campaignDay.getDate());
        return formatted;
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        Marker marker = (Marker) event.getOverlay();
        Location location = (Location) marker.getData();
        boolean selected = isLocationSelected(location);
        if (selected) {
            actionUnselectLocation(location);
        } else {
            actionSelectLocation(location);
        }
    }

    public LocationSearch getLocationSearch() {
        return locationSearch;
    }

    public LazyDataModel<Location> getLocationsModel() {
        return locationsModel;
    }

    public MapModel getGoogleMapModel() {
        return googleMapModel;
    }

    public Step getCurrentStep() {
        return currentStep;
    }

    public List<CampaignDay> getAllDates() {
        return allDates;
    }

    public Integer getPersonAmount() {
        return personAmount;
    }

    public void setPersonAmount(Integer personAmount) {
        this.personAmount = personAmount;
    }

    private void searchLocations() {
        checkLocationSearch();
        locationsModel = new LocationModel();
        updateMarkers();
    }

    private void searchAllCampaignDates() {
        allDates = campaignDatesService.findAllDays();
    }

    private void updateMarkers() {
        locationSearch.setPagination(null);
        List<Location> locations = locationService.findLocations(locationSearch);
        googleMapModel.getMarkers().clear();

        for (Location location : locations) {
            BigDecimal latitude = location.getLatitude();
            BigDecimal longitude = location.getLongitude();
            String name = location.getName();
            LatLng latLng = new LatLng(latitude.floatValue(), longitude.floatValue());
            Marker marker = new Marker(latLng, name);
            marker.setData(location);

            boolean selected = isLocationSelected(location);
            if (selected) {
                marker.setIcon("https://maps.google.com/mapfiles/kml/paddle/red-stars.png");
            } else {
                marker.setIcon("https://maps.google.com/mapfiles/kml/paddle/wht-circle.png");
            }
            googleMapModel.addOverlay(marker);
        }
    }

    private void checkLocationSearch() {
        String cityLike = locationSearch.getCityLike();
        if (cityLike != null && cityLike.trim().isEmpty()) {
            locationSearch.setCityLike(null);
        }
        String nameLike = locationSearch.getNameLike();
        if (nameLike != null && nameLike.trim().isEmpty()) {
            locationSearch.setNameLike(null);
        }
        String postalCode = locationSearch.getPostalCode();
        if (postalCode != null && postalCode.trim().isEmpty()) {
            locationSearch.setPostalCode(null);
        }
    }

    private class LocationModel extends MyLazyDataModel<Location> {

        @Override
        protected List<Location> simpleLoad(Pagination pagination) {
            locationSearch.setPagination(pagination);
            Long locationsCount = locationService.countLocation(locationSearch);
            setRowCount(locationsCount.intValue());

            List<Location> locations = locationService.findLocations(locationSearch);
            return locations;
        }

    }
}
