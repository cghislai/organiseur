/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.UserService;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class UserController implements Serializable {

    @EJB
    private UserService userService;
    @Inject
    private AuthController authController;

    private String password1;
    private String password2;

    @PostConstruct
    public void init() {
        password1 = null;
        password2 = null;
    }

    public void actionSave() {
        User authenticatedUser = authController.getAuthenticatedUser();
        String encodedPassword = LoginController.encodePassword(password1);
        authenticatedUser.setPasswordHash(encodedPassword);
        userService.saveUser(authenticatedUser);
        
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_INFO, "Enregistré", null);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, facesMessage);
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

}
