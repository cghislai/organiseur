/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.service.UserService;
import com.cghislai.organiseurilesdepaix.service.search.UserSearch;
import com.cghislai.organiseurilesdepaix.view.Views;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
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
public class LoginController implements Serializable {

    @EJB
    private UserService userService;
    @Inject
    private AuthController authController;
    
    private String userNameOrMail;
    private String password;

    public String actionShow() {
        return Views.USER_LOGIN;
    }

    public String actionLogin() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        HttpServletRequest request = (HttpServletRequest) externalContext.getRequest();
        try {
            String encodedPass = encodePassword(password);
            request.login(userNameOrMail, encodedPass);
            
            UserSearch userSearch = new UserSearch();
            userSearch.setLoginOrMail(userNameOrMail);
            User user = userService.searchUser(userSearch);
            authController.actionAuthenticateUser(user);
            return Views.INDEX;
        } catch (ServletException ex) {
            onLoginFail(ex);
            return null;
        }
    }

    private void onLoginFail(Exception exception) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Impossible d'ouvrir une session", "Vérifiez que le nom d'utilisateur et le mot de passe soient corrects");
        facesContext.addMessage(null, facesMessage);
    }

    public static String encodePassword(String password) {
        try {
            if (password == null || password.trim().isEmpty()) {
                return password;
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(password.getBytes("UTF-8"));
            byte[] base64Encoded = Base64.getEncoder().encode(encoded);
            String encodedString = new String(base64Encoded, "UTF-8");
            
            return encodedString;
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(LoginController.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public String getUserNameOrMail() {
        return userNameOrMail;
    }

    public void setUserNameOrMail(String userNameOrMail) {
        this.userNameOrMail = userNameOrMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
