/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.UserService;
import com.cghislai.organiseurilesdepaix.view.Views;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class UserRegisterController implements Serializable {

    @EJB
    private UserService userService;
    @Inject
    private AuthController authController;

    private String login;
    private String humanName;
    private String email;
    private String telephone;
    private String password1;
    private String password2;

    public String actionRegister() {
        if (!password1.equals(password2)) {
            showError("Les mots de passe ne correspondent pas");
            return null;
        }

        String encodedPassword = LoginController.encodePassword(password1);

        User newUser = new User();
        newUser.setAdmin(false);
        newUser.setEmail(email);
        newUser.setPasswordHash(encodedPassword);
        newUser.setUserName(login);
        newUser.setHumanName(humanName);
        newUser.setTelephone(telephone);
        User managedUser = userService.saveUser(newUser);

        // Login
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            request.login(login, encodedPassword);
            authController.actionAuthenticateUser(managedUser);
        } catch (ServletException ex) {
            showError("Impossible d'ouvrir la session");
            return Views.USER_LOGIN;
        }
        return Views.INDEX;
    }

    private void showError(String message) {
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null);
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.addMessage(null, facesMessage);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getHumanName() {
        return humanName;
    }

    public void setHumanName(String humanName) {
        this.humanName = humanName;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

}
