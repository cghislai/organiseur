/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service.search;

import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import java.io.Serializable;

/**
 *
 * @author cghislai
 */
public class UserSearch implements Serializable {

    private Long userId;
    private String loginOrMail;
    private String login;
    private String mail;
    private String nameLike;
    private Boolean admin;
    private Pagination pagination;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLoginOrMail() {
        return loginOrMail;
    }

    public void setLoginOrMail(String loginOrMail) {
        this.loginOrMail = loginOrMail;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

}
