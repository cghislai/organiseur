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
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.LocationTimeSlotService;
import com.cghislai.organiseurilesdepaix.service.SubscriptionService;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.service.search.LocationTimeSlotSearch;
import com.cghislai.organiseurilesdepaix.service.search.SubscriptionSearch;
import com.cghislai.organiseurilesdepaix.view.domain.LocationDayWithSlotsStatus;
import com.cghislai.organiseurilesdepaix.view.domain.SlotStatus;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Marker;

/**
 *
 * @author cghislai
 */
@Named
@SessionScoped
public class AvailabilitiesController implements Serializable {

    @Inject
    private AuthController authController;
    @EJB
    private SubscriptionService subscriptionService;
    @EJB
    private LocationService locationService;
    @EJB
    private LocationTimeSlotService locationTimeSlotService;
    @EJB
    private CampaignDatesService campaignDatesService;
    @Inject
    private TimeSlotController timeSlotController;
    @Inject
    private CampaignDatesController campaignDatesController;

    private LazyDataModel<Location> locationsModel;
    private Location selectedLocation;
    private LocationSearch locationSearch;
    private DefaultMapModel googleMapModel;

    private List<CampaignDay> allCampaignDays;

    private Map<CampaignDay, LocationDayWithSlotsStatus> slotsStatusMap;

    @PostConstruct
    public void init() {
        locationSearch = new LocationSearch();
        googleMapModel = new DefaultMapModel();
        searchLocations();
        searchCampaignDays();
        searchSlotsStatus();
    }

    private void searchCampaignDays() {
        allCampaignDays = campaignDatesService.findAllDays();
    }

    public void searchLocations() {
        checkLocationSearch();
        locationsModel = new LocationsModel();
        updateMarkers();
    }

    public void actionSelectLocation(Location location) {
        this.selectedLocation = location;
        this.searchSlotsStatus();
        updateMarkers();
    }

    public void searchSlotsStatus() {
        slotsStatusMap = null;
        if (selectedLocation == null) {
            return;
        }
        slotsStatusMap = new HashMap<>();
        allCampaignDays.stream().forEach((campaignDay) -> {
            Map<Long, SlotStatus> slotsMap = createSlotsStatusMap(campaignDay);
            LocationDayWithSlotsStatus locationWithSlots = new LocationDayWithSlotsStatus();
            locationWithSlots.setCampaignDay(campaignDay);
            locationWithSlots.setLocation(selectedLocation);
            locationWithSlots.setSlots(slotsMap);
            slotsStatusMap.put(campaignDay, locationWithSlots);
        });
    }

    private Map<Long, SlotStatus> createSlotsStatusMap(CampaignDay campaignDay1) {
        List<Long> timeSlots = timeSlotController.getAllSlots();
        Map<Long, SlotStatus> slotsMap = new HashMap<>();
        timeSlots.stream().forEach((Long time) -> {
            createSlotStatus(campaignDay1, time, slotsMap);
        });
        return slotsMap;
    }

    private void createSlotStatus(CampaignDay campaignDay1, Long time, Map<Long, SlotStatus> slotsMap) throws FacesException {
        LocationTimeSlotSearch locationTimeSlotSearch = new LocationTimeSlotSearch();
        locationTimeSlotSearch.setCampaignDay(campaignDay1);
        locationTimeSlotSearch.setLocation(selectedLocation);
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
        subscriptionSearch.setCampaignDay(campaignDay1);
        subscriptionSearch.setLocation(selectedLocation);
        subscriptionSearch.setStartHour(time);
        Long subscriptionsCount = subscriptionService.countSubscriptions(subscriptionSearch);
        List<Subscription> subscriptions = subscriptionService.searchSubscriptions(subscriptionSearch, new Pagination(0, 5));

        subscriptionSearch.setUser(authController.getAuthenticatedUser());
        Long userSubscriptionCount = subscriptionService.countSubscriptions(subscriptionSearch);
        boolean userSubscribed = userSubscriptionCount > 0;

        String timeLabel = timeSlotController.getSlotLabel(time);
        String amountLabel = (subscriptionsCount == 0 ? "Aucune" : subscriptionsCount)
                + " personne"
                + (subscriptionsCount > 1 ? "s" : "");
        String subscribedLabel = (userSubscribed ? "(Inscrit)" : "");
        String label = timeLabel + "<br>" + amountLabel + "<br>" + subscribedLabel;
        String usersLabel = subscriptions.stream()
                .map(subscription -> subscription.getUser().getHumanName())
                .reduce("", (userNames, userName) -> (userNames.isEmpty() ? "" : userNames + "<br>") + userName);
        if (subscriptions.size() < subscriptionsCount) {
            long diff = subscriptionsCount - subscriptions.size();
            usersLabel += "<br>Et " + diff + " autre" + (diff > 1 ? "s" : "");
        }
        SlotStatus slotStatus = new SlotStatus();
        slotStatus.setSubscriptionAmount(subscriptionsCount);
        slotStatus.setTimeSlot(locationSlot);
        slotStatus.setLabel(label);
        slotStatus.setTooltip(usersLabel);
        slotStatus.setUserSubscribed(userSubscribed);

        long otherPersonAmount = subscriptionsCount;
        if (userSubscribed) {
            otherPersonAmount--;
        }
        slotStatus.setEditable(otherPersonAmount < 2);
        slotsMap.put(time, slotStatus);
    }

    private void updateMarkers() {
        locationSearch.setPagination(null);
        List<Location> locations = locationService.findLocations(locationSearch);
        googleMapModel.getMarkers().clear();

        if (locations.isEmpty()) {
            return;
        }

        locations.stream().map((location) -> {
            BigDecimal latitude = location.getLatitude();
            BigDecimal longitude = location.getLongitude();
            if (latitude == null || longitude == null) {
                return null;
            }
            String name = location.getName();
            LatLng latLng = new LatLng(latitude.floatValue(), longitude.floatValue());
            Marker marker = new Marker(latLng, name);
            marker.setData(location);
            boolean selected = location.equals(selectedLocation);
            if (selected) {
                marker.setIcon("https://maps.google.com/mapfiles/kml/paddle/red-stars.png");
            } else {
                marker.setIcon("https://maps.google.com/mapfiles/kml/paddle/wht-circle.png");
            }
            return marker;
        }).forEach((marker) -> {
            if (marker != null) {
                googleMapModel.addOverlay(marker);
            }
        });
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        Marker marker = (Marker) event.getOverlay();
        Location location = (Location) marker.getData();
        boolean selected = location.equals(selectedLocation);
        if (selected) {
            actionSelectLocation(null);
        } else {
            actionSelectLocation(location);
        }
    }

    public void onSubscribedChanged(SlotStatus slotStatus, boolean subscribed) {
        if (slotStatus == null || !slotStatus.isEditable()) {
            return;
        }
        LocationTimeSlot timeSlot = slotStatus.getTimeSlot();
        SubscriptionSearch subscriptionSearch = new SubscriptionSearch();
        subscriptionSearch.setCampaignDay(timeSlot.getCampaignDay());
        subscriptionSearch.setStartHour(timeSlot.getStartTime());
        subscriptionSearch.setUser(authController.getAuthenticatedUser());
        if (subscribed) {
            long subscriptionCount = subscriptionService.countSubscriptions(subscriptionSearch);
            if (subscriptionCount > 0) {
                List<Subscription> subscriptions = subscriptionService.searchSubscriptions(subscriptionSearch, null);
                for (Subscription subscription : subscriptions) {
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    FacesMessage facesMessage = new FacesMessage();
                    String title = "Déjà inscrit";
                    String label = "Déjà inscrit le " + campaignDatesController.getCampaignDateLabel(subscription.getLocationTimeSlot().getCampaignDay());
                    label += " pour " + timeSlotController.getSlotLabel(subscription.getLocationTimeSlot().getStartTime());
                    label += " à l'emplacement " + subscription.getLocationTimeSlot().getLocation().getName();
                    facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
                    facesMessage.setSummary(title);
                    facesMessage.setDetail(label);
                    facesContext.addMessage(null, facesMessage);
                }
                return;
            }
            Subscription subscription = new Subscription();
            subscription.setLocationTimeSlot(timeSlot);
            subscription.setUser(authController.getAuthenticatedUser());
            subscriptionService.saveSubscription(subscription);
        } else {
            subscriptionSearch.setLocation(selectedLocation);
            List<Subscription> subscriptions = subscriptionService.searchSubscriptions(subscriptionSearch, null);
            if (subscriptions.size() != 1) {
                throw new FacesException("Not a single subscription");
            }
            subscriptionService.deleteSubscription(subscriptions.get(0));
        }
        searchSlotsStatus();
        authController.searchSubscribed();
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

    public List<CampaignDay> getAllCampaignDays() {
        return allCampaignDays;
    }

    public Map<CampaignDay, LocationDayWithSlotsStatus> getSlotsStatusMap() {
        return slotsStatusMap;
    }

    public DefaultMapModel getGoogleMapModel() {
        return googleMapModel;
    }

    public LocationSearch getLocationSearch() {
        return locationSearch;
    }

    public void setLocationSearch(LocationSearch locationSearch) {
        this.locationSearch = locationSearch;
    }

    public LazyDataModel<Location> getLocationsModel() {
        return locationsModel;
    }

    public Location getSelectedLocation() {
        return selectedLocation;
    }

    private class LocationsModel extends LazyDataModel<Location> {

        @Override
        public List<Location> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
            return loadData(first, pageSize);
        }

        @Override
        public List<Location> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
            return loadData(first, pageSize);
        }

        private List<Location> loadData(int first, int pageSize) {
            Pagination pagination = new Pagination(first, pageSize);
            locationSearch.setPagination(pagination);

            Long locationCount = locationService.countLocation(locationSearch);
            setRowCount(locationCount.intValue());

            List<Location> locaitons = locationService.findLocations(locationSearch);
            return locaitons;
        }

    }

}
