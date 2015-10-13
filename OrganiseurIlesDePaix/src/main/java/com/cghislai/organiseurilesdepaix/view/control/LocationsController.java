/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.LocationServiceUtils;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.util.MyLazyDataModel;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
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
public class LocationsController implements Serializable {

    @EJB
    private LocationService locationService;
    @EJB
    private LocationServiceUtils locationServiceUtils;

    private LazyDataModel<Location> locationDataModel;
    private LocationSearch locationSearch;
    private Location editingLocation;
    private MapModel mapModel;

    @PostConstruct
    public void init() {
        locationSearch = new LocationSearch();
        mapModel = new DefaultMapModel();
        search();
    }

    public void actionNewLocation() {
        editingLocation = new Location();
    }

    public void actionEditLocation(Location location) {
        editingLocation = location;
    }

    public boolean isEditing() {
        return editingLocation != null;
    }

    public void actionCancelEdit() {
        editingLocation = null;
    }

    public void actionSaveLocation() {
        locationService.saveLocation(editingLocation);
        editingLocation = null;
        search();
    }

    public void actionSeachCoordinates() {
        Float[] coordinates = searchCoordinates(editingLocation);
        if (coordinates == null) {
            return;
        }
        BigDecimal latitude = BigDecimal.valueOf(coordinates[0]);
        BigDecimal longitude = BigDecimal.valueOf(coordinates[1]);
        editingLocation.setLatitude(latitude);
        editingLocation.setLongitude(longitude);
    }

    private Float[] searchCoordinates(Location location) {
        try {
            Future<Float[]> asyncResult = locationServiceUtils.findLatLongAsync(location);
            Float[] coordinates = asyncResult.get();
            if (coordinates == null) {
                onCoordinatesLookupFailed();
            } else {
                return coordinates;
            }
        } catch (InterruptedException | ExecutionException ex) {
            onCoordinatesLookupFailed();
        } catch (MalformedURLException ex) {
            onCoordinatesLookupFailed();
        } catch (IOException ex) {
            onCoordinatesLookupFailed();
        }
        return null;
    }

    private void onCoordinatesLookupFailed() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Impossible de localiser l'adresse", null);
        facesContext.addMessage("editForm", facesMessage);
    }

    public void actionDeleteLocation(Location location) {
        locationService.removeLocation(location);
        search();
    }

    public void onMarkerSelect(OverlaySelectEvent event) {
        Marker marker = (Marker) event.getOverlay();
        String title = marker.getTitle();
        LocationSearch markerSearch = new LocationSearch();
        markerSearch.setNameLike(title);
        List<Location> locations = locationService.findLocations(markerSearch);
        if (locations.size() == 1) {
            Location location = locations.get(0);
            actionEditLocation(location);
        }
    }

    private void search() {
        locationDataModel = new LocationDataModel();
    }

    public LazyDataModel<Location> getLocationDataModel() {
        return locationDataModel;
    }

    public LocationSearch getLocationSearch() {
        return locationSearch;
    }

    public Location getEditingLocation() {
        return editingLocation;
    }

    public MapModel getMapModel() {
        return mapModel;
    }

    private class LocationDataModel extends MyLazyDataModel<Location> {

        @Override
        protected List<Location> simpleLoad(Pagination pagination) {
            locationSearch.setPagination(pagination);
            Long count = locationService.countLocation(locationSearch);
            setRowCount(count.intValue());

            List<Location> locations = locationService.findLocations(locationSearch);
            mapModel.getMarkers().clear();

            for (Location location : locations) {
                if (location.getLatitude() == null
                        || location.getLongitude() == null) {
                    continue;
                }
                LatLng latLng = new LatLng(location.getLatitude().floatValue(), location.getLongitude().floatValue());
                Marker marker = new Marker(latLng, location.getName());
                mapModel.addOverlay(marker);
            }
            return locations;
        }

    }

}
