/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.AvailabilityService;
import com.cghislai.organiseurilesdepaix.service.search.AvailabilitySearch;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author cghislai
 */
@Named
@SessionScoped
public class UserSessionController implements Serializable {

    @Inject
    private AuthController authController;
    @EJB
    private AvailabilityService availabilityService;

    private boolean hasAvailabilities;

    @PostConstruct
    public void init() {
        updateAvailabilities();
    }

    public void updateAvailabilities() {
        User authenticatedUser = authController.getAuthenticatedUser();
        if (authenticatedUser == null) {
            hasAvailabilities = false;
            return;
        }
        AvailabilitySearch availabilitySearch = new AvailabilitySearch();
        availabilitySearch.setUser(authenticatedUser);
        Long count = availabilityService.countAvailabilities(availabilitySearch);
        hasAvailabilities = count > 0;
    }

    public boolean isHasAvailabilities() {
        return hasAvailabilities;
    }

}
