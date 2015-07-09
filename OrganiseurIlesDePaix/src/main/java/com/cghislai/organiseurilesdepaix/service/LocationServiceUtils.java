/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.Location;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.concurrent.Future;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;

/**
 *
 * @author cghislai
 */
@Stateless
public class LocationServiceUtils {

    @Asynchronous
    public Future<Float[]> findLatLongAsync(Location location) throws UnsupportedEncodingException, MalformedURLException, IOException {
        String urlPath = " http://nominatim.openstreetmap.org/";
        urlPath += "?format=json";
        urlPath += "&addressdetails=1";
        urlPath += "&limit=1";
        String queryString = location.getAddress();
        queryString += "," + location.getPostalCode() + " " + location.getCity();
        queryString += ", Belgium";
        String encodedQuery = URLEncoder.encode(queryString, "UTF-8");
        urlPath += "&q=" + encodedQuery;

        URL url = new URL(urlPath);
        System.out.println(urlPath);
        URLConnection connection = url.openConnection();

        connection.setDoInput(true);

        //Get Response  
        InputStream is = connection.getInputStream();
        JsonReader jsonReader = Json.createReader(is);
        JsonArray jsonArray = jsonReader.readArray();

        int size = jsonArray.size();
        if (size != 1) {
            return null;
        }
        JsonObject jsonObject = jsonArray.getJsonObject(0);
        JsonString latitude = jsonObject.getJsonString("lat");
        JsonString longitude = jsonObject.getJsonString("lon");
        JsonString displayName = jsonObject.getJsonString("display_name");
        System.out.println(displayName.getString());
        
        Float latitudeFloat = Float.valueOf(latitude.getString());
        Float longitudeFloat = Float.valueOf(longitude.getString());

        Float[] coordinates = new Float[]{latitudeFloat, longitudeFloat};
        return new AsyncResult<>(coordinates);
    }
}
