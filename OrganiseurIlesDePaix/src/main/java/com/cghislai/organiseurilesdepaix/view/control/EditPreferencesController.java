/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.service.GlobalPreferenceService;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class EditPreferencesController implements Serializable {

    @EJB
    private GlobalPreferenceService globalPreferenceService;
    @Inject
    private GlobalPreferencesController globalPreferencesController;

    private String contactName;
    private String contactMail;
    private String contactPhone1;
    private String contactPhone2;

    @PostConstruct
    public void init() {
        search();
    }

    private void search() {
        contactMail = globalPreferenceService.getPreferenceValue(GlobalPreferenceService.KEY_SITE_CONTACT_EMAIL);
        contactName = globalPreferenceService.getPreferenceValue(GlobalPreferenceService.KEY_SITE_CONTACT_NAME);
        contactPhone1 = globalPreferenceService.getPreferenceValue(GlobalPreferenceService.KEY_SITE_CONTACT_TELEPHONE1);
        contactPhone2 = globalPreferenceService.getPreferenceValue(GlobalPreferenceService.KEY_SITE_CONTACT_TELEPHONE2);
    }

    public void actionSaveContact() {
        globalPreferenceService.setPreference(GlobalPreferenceService.KEY_SITE_CONTACT_NAME, contactName);
        globalPreferenceService.setPreference(GlobalPreferenceService.KEY_SITE_CONTACT_EMAIL, contactMail);
        globalPreferenceService.setPreference(GlobalPreferenceService.KEY_SITE_CONTACT_TELEPHONE1, contactPhone1);
        globalPreferenceService.setPreference(GlobalPreferenceService.KEY_SITE_CONTACT_TELEPHONE2, contactPhone2);
        search();
    }
    public void actionSetRegistrationClosed(boolean value) {
        globalPreferencesController.setPreferenceBoolean(GlobalPreferenceService.KEY_REGISTRATION_CLOSED, value);
        
    }

    public GlobalPreferencesController getGlobalPreferencesController() {
        return globalPreferencesController;
    }

    public void setGlobalPreferencesController(GlobalPreferencesController globalPreferencesController) {
        this.globalPreferencesController = globalPreferencesController;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactMail() {
        return contactMail;
    }

    public void setContactMail(String contactMail) {
        this.contactMail = contactMail;
    }

    public String getContactPhone1() {
        return contactPhone1;
    }

    public void setContactPhone1(String contactPhone1) {
        this.contactPhone1 = contactPhone1;
    }

    public String getContactPhone2() {
        return contactPhone2;
    }

    public void setContactPhone2(String contactPhone2) {
        this.contactPhone2 = contactPhone2;
    }

}
