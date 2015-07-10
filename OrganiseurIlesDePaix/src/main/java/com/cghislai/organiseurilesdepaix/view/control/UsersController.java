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

    private void search() {
        usersModel = new UsersModel();
    }

    public UserSearch getUserSearch() {
        return userSearch;
    }

    public LazyDataModel<User> getUsersModel() {
        return usersModel;
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
