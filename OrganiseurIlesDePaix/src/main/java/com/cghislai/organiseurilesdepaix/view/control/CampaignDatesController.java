/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.service.CampaignDatesService;
import com.cghislai.organiseurilesdepaix.service.search.CampaignDaySearch;
import com.cghislai.organiseurilesdepaix.util.DateUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class CampaignDatesController implements Serializable {

    @EJB
    private CampaignDatesService campaignDatesService;

    private Date toAddDate;
    private List<CampaignDay> campaignDays;

    @PostConstruct
    public void init() {
        search();
    }

    private void search() {
        campaignDays = campaignDatesService.findAllDays();
    }

    public void actionAddDate() {
        if (toAddDate == null) {
            return;
        }
        Date dayDate = DateUtils.dayOnly(toAddDate);
        CampaignDaySearch daySearch = new CampaignDaySearch();
        daySearch.setDate(dayDate);
        Long existingCount = campaignDatesService.countCampaignDays(daySearch);
        if (existingCount > 0) {
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Date existante", null);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.addMessage(null, facesMessage);
            return;
        }
        CampaignDay campaignDay = new CampaignDay();
        campaignDay.setDate(dayDate);
        CampaignDay managedDay = campaignDatesService.saveCampaignDay(campaignDay);
        toAddDate = null;
        search();
    }

    public void actionRemoveDate(CampaignDay campaignDay) {
        campaignDatesService.removeCampaignDay(campaignDay);
        search();
    }

    public Date getToAddDate() {
        return toAddDate;
    }

    public void setToAddDate(Date toAddDate) {
        this.toAddDate = toAddDate;
    }

    public List<CampaignDay> getCampaignDays() {
        return campaignDays;
    }

}
