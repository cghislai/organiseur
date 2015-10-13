/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view;

/**
 *
 * @author cghislai
 */
public class Views {

    private static final String EXTENTION_REDIRECT = ".jsf?faces-redirect=true";

    public static final String INDEX = "/index" + EXTENTION_REDIRECT;
    public static final String USER_LOGIN = "/user/login" + EXTENTION_REDIRECT;
    public static final String USER_REGISTER_AVAILABILITY_PERSON = "/user/availabilities_person" + EXTENTION_REDIRECT;
    public static final String USER_REGISTER_AVAILABILITY_DATES = "/user/availabilities_dates" + EXTENTION_REDIRECT;
    public static final String USER_REGISTER_AVAILABILITY_LOCATION = "/user/availabilities_locations" + EXTENTION_REDIRECT;
    public static final String USER_AVAILABILITIES = "/user/availabilities" + EXTENTION_REDIRECT;
    public static final String TOOLS_LOCATIONS = "/tools/locations" + EXTENTION_REDIRECT;
}
