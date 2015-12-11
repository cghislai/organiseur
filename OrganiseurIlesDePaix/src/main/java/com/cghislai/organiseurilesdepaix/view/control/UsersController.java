/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.control;

import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.UserService;
import com.cghislai.organiseurilesdepaix.service.search.UserSearch;
import com.cghislai.organiseurilesdepaix.util.MyLazyDataModel;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;

/**
 *
 * @author cghislai
 */
@Named
@ViewScoped
public class UsersController implements Serializable {

    @EJB
    private UserService userService;

    private UserSearch userSearch;
    private LazyDataModel<User> usersModel;

    private User editingUser;
    private String password1;
    private String password2;

    @PostConstruct
    public void init() {
        userSearch = new UserSearch();
        search();
    }

    public void actionSearch() {
        search();
    }

    public void actionDelete(User user) {
        userService.removeUser(user);
        search();
    }

    public void actionEdit(User user) {
        this.editingUser = user;
        this.password1 = null;
        this.password2 = null;
    }

    public void actionSaveEdit() {
        if (password1 != null || password2 != null) {
            if (password1 == null || !password1.equals(password2)) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                facesContext.addMessage(null, new FacesMessage("Mots de passe incorrects"));
            }
        }
        String encodedPassword = LoginController.encodePassword(password1);
        editingUser.setPasswordHash(encodedPassword);
        this.userService.saveUser(editingUser);
        this.editingUser = null;
    }

    public void actionCancelEdit() {
        this.editingUser = null;
    }

    private void search() {
        usersModel = new UsersModel();
    }

    public UserSearch getUserSearch() {
        return userSearch;
    }

    public LazyDataModel<User> getUsersModel() {
        return usersModel;
    }

    public User getEditingUser() {
        return editingUser;
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

    private class UsersModel extends MyLazyDataModel<User> {

        @Override
        protected List<User> simpleLoad(Pagination pagination) {
            userSearch.setPagination(pagination);

            Long userCount = userService.countUsers(userSearch);
            setRowCount(userCount.intValue());

            List<User> users = userService.searchUsers(userSearch);
            return users;
        }

    }
}
