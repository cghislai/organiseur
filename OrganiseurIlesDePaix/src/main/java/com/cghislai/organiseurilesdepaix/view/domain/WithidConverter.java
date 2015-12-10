/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.view.domain;

import com.cghislai.organiseurilesdepaix.domain.WithId;
import java.util.HashMap;
import java.util.Map;
import javax.enterprise.context.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author cghislai
 */
@FacesConverter(value = "withIdConverter")
public class WithidConverter implements Converter {

    private static final Map<Long, WithId> objectMaps = new HashMap<>();

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        String[] splitted = value.split(":");
        if (splitted.length != 2) {
            throw new ConverterException("Invalid string vlaue : " + value);
        }
        Long id = Long.parseLong(splitted[1]);
        return objectMaps.get(id);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (!(value instanceof WithId)) {
            throw new ConverterException("Not a withId");
        }
        WithId withid = (WithId) value;
        String stringValue = value.getClass().getSimpleName() + ":" + withid.getId();
        objectMaps.put(withid.getId(), withid);
        return stringValue;
    }

}
