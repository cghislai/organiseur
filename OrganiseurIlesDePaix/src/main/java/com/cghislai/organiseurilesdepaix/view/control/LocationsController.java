/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.LocationService;
import com.cghislai.organiseurilesdepaix.service.search.LocationSearch;
import com.cghislai.organiseurilesdepaix.util.MyLazyDataModel;
import java.io.Serializable;
import java.security.Principal;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class LocationsController implements Serializable {

    @EJB
    private LocationService locationService;

    private LazyDataModel<Location> locationDataModel;
    private LocationSearch locationSearch;
    private Location editingLocation;

    @PostConstruct
    public void init() {
        locationSearch = new LocationSearch();
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

    public void actionDeleteLocation(Location location) {
        locationService.removeLocation(location);
        search();
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

    private class LocationDataModel extends MyLazyDataModel<Location> {

        @Override
        protected List<Location> simpleLoad(Pagination pagination) {
            locationSearch.setPagination(pagination);
            Long count = locationService.countLocation(locationSearch);
            setRowCount(count.intValue());

            List<Location> locations = locationService.findLocations(locationSearch);
            return locations;
        }

    }

}
