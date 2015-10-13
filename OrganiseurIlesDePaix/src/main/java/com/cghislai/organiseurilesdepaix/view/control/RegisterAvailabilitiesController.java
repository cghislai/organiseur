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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
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
@SessionScoped
public class RegisterAvailabilitiesController implements Serializable {

    @EJB
    private CampaignDatesService campaignDatesService;
    @EJB
    private LocationService locationService;
    @EJB
    private AvailabilityService availabilityService;
    @Inject
    private AuthController authController;
    @Inject
    private UserSessionController userSessionController;

    private List<DayTimeAvailability> dayTimeAvailabilities;
    private Long personAmount;
    private LocationSearch locationSearch;
    private List<Location> selectedLocations;
    private LazyDataModel<Location> locationsModel;
    private MapModel googleMapModel;

    public RegisterAvailabilitiesController() {
    }

    @PostConstruct
    public void init() {
        personAmount = 1L;
        List<CampaignDay> allDates = campaignDatesService.findAllDays();
        dayTimeAvailabilities = allDates.stream()
                .map((CampaignDay campaignDay) -> {
                    DayTimeAvailability availability = new DayTimeAvailability();
                    availability.setSelected(false);
                    availability.setCampaignDay(campaignDay);
                    Date date = availability.getCampaignDay().getDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    calendar.set(Calendar.HOUR_OF_DAY, 8);
                    Date startTime = calendar.getTime();
                    calendar.set(Calendar.HOUR_OF_DAY, 18);
                    Date endTime = calendar.getTime();
                    availability.setStartTime(startTime);
                    availability.setEndTime(endTime);
                    return availability;
                })
                .collect(Collectors.toList());
        locationSearch = new LocationSearch();
        selectedLocations = new ArrayList<>();
        googleMapModel = new DefaultMapModel();
    }

    public boolean isAnyDateSelected() {
        return dayTimeAvailabilities.stream()
                .filter(availability -> availability.getSelected())
                .findAny()
                .isPresent();
    }

    public boolean isAnyLocationSelected() {
        return selectedLocations.size() > 0;
    }

    public String actionRegister() {
        User authenticatedUser = authController.getAuthenticatedUser();
        for (DayTimeAvailability dayTimeAvailability : dayTimeAvailabilities) {
            selectedLocations.stream().map((location) -> {
                Date startDate = dayTimeAvailability.getStartTime();
                Date endDate = dayTimeAvailability.getEndTime();
                startDate = DateUtils.timeOnly(startDate);
                endDate = DateUtils.timeOnly(endDate);
                Availability disponibility = new Availability();
                disponibility.setCampaignDay(dayTimeAvailability.getCampaignDay());
                disponibility.setEndTime(endDate);
                disponibility.setLocation(location);
                disponibility.setPersonAmount(personAmount.intValue());
                disponibility.setStartTime(startDate);
                disponibility.setUser(authenticatedUser);
                return disponibility;
            }).forEach((disponibility) -> {
                availabilityService.saveAvailability(disponibility);
            });
        }
        userSessionController.updateAvailabilities();
        return Views.USER_AVAILABILITIES;
    }

    public String actionShow() {
        return actionShowPerson();
    }

    public String actionShowPerson() {
        return Views.USER_REGISTER_AVAILABILITY_PERSON;
    }

    public String actionShowDates() {
        return Views.USER_REGISTER_AVAILABILITY_DATES;
    }

    public String actionShowLocations() {
        searchLocations();
        return Views.USER_REGISTER_AVAILABILITY_LOCATION;
    }

    public List<DayTimeAvailability> getDayTimeAvailabilities() {
        return dayTimeAvailabilities;
    }

    public Long getPersonAmount() {
        return personAmount;
    }

    public void setPersonAmount(Long personAmount) {
        this.personAmount = personAmount;
    }

    public void actionSearchLocations() {
        searchLocations();
    }

    private void searchLocations() {
        checkLocationSearch();
        locationsModel = new LocationModel();
        updateMarkers();
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

    public LocationSearch getLocationSearch() {
        return locationSearch;
    }

    public LazyDataModel<Location> getLocationsModel() {
        return locationsModel;
    }

    public MapModel getGoogleMapModel() {
        return googleMapModel;
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
