/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.view.Views;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author cghislai
 */
@Named
@SessionScoped
public class AuthController implements Serializable {

    private User authenticatedUser;

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void actionAuthenticateUser(User user) {
        authenticatedUser = user;
    }

    public String actionDeauthenticateUser() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            request.logout();
            authenticatedUser = null;
        } catch (ServletException ex) {
            Logger.getLogger(AuthController.class.getName()).log(Level.SEVERE, null, ex);
        }
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        return Views.INDEX;
    }

    public boolean isUserAuthenticated() {
        return authenticatedUser != null;
    }

    public boolean isAdminAuthenticated() {
        return authenticatedUser != null && authenticatedUser.isAdmin();
    }
}
