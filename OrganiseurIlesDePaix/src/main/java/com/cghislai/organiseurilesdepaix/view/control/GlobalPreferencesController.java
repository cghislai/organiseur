/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.GlobalPreference;
import com.cghislai.organiseurilesdepaix.service.GlobalPreferenceService;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 *
 * @author cghislai
 */
@Named
@ApplicationScoped
public class GlobalPreferencesController implements Serializable {

    @EJB
    private GlobalPreferenceService globalPreferenceService;

    private Boolean registrationClosed;
    private Boolean scheduleOnline;

    @PostConstruct
    public void init() {
        searchPreferences();
    }

    private void searchPreferences() {
        GlobalPreference registrationClosedPref = globalPreferenceService.getGlobalPreference(GlobalPreferenceService.KEY_REGISTRATION_CLOSED);
        if (registrationClosedPref == null) {
            globalPreferenceService.setPreferenceBoolean(GlobalPreferenceService.KEY_REGISTRATION_CLOSED, true);
            registrationClosed = true;
        } else {
            registrationClosed = globalPreferenceService.getPreferenceBooleanValue(GlobalPreferenceService.KEY_REGISTRATION_CLOSED);
        }
        scheduleOnline = globalPreferenceService.getPreferenceBooleanValue(GlobalPreferenceService.KEY_SCHEDULE_ONLINE);
    }

    public void setPreferenceBoolean(String key, Boolean value) {
        globalPreferenceService.setPreferenceBoolean(key, value);
        searchPreferences();
    }

    public void removeGlobalPreference(String key) {
        globalPreferenceService.removeGlobalPreference(key);
        searchPreferences();
    }

    public Boolean getRegistrationClosed() {
        return registrationClosed;
    }

    public Boolean getScheduleOnline() {
        return scheduleOnline;
    }

}
