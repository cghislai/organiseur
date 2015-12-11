/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.GlobalPreference;
import com.cghislai.organiseurilesdepaix.domain.GlobalPreference_;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

/**
 *
 * @author cghislai
 */
@Stateless
public class GlobalPreferenceService {

    public static final String KEY_SITE_CONTACT_NAME = "contact name";
    public static final String KEY_SITE_CONTACT_EMAIL = "contact mail";
    public static final String KEY_SITE_CONTACT_TELEPHONE1 = "contact telephone1";
    public static final String KEY_SITE_CONTACT_TELEPHONE2 = "contact telephone2";
    public static final String KEY_REGISTRATION_CLOSED = "registration closed";
    public static final String KEY_SCHEDULE_ONLINE = "schedule online";

    @PersistenceContext
    private EntityManager entityManager;

    public GlobalPreference getGlobalPreference(String key) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<GlobalPreference> query = builder.createQuery(GlobalPreference.class);
        Root<GlobalPreference> root = query.from(GlobalPreference.class);

        Path<String> keyPath = root.get(GlobalPreference_.key);
        query.select(root);
        query.where(builder.equal(keyPath, key));

        TypedQuery<GlobalPreference> typedQuery = entityManager.createQuery(query);
        try {
            GlobalPreference pref = typedQuery.getSingleResult();
            return pref;
        } catch (NoResultException e) {
            return null;
        }
    }

    public String getPreferenceValue(String key) {
        GlobalPreference preference = getGlobalPreference(key);
        if (preference == null) {
            return null;
        }
        return preference.getValue();
    }

    public Boolean getPreferenceBooleanValue(String key) {
        String stringValue = getPreferenceValue(key);
        if (stringValue == null) {
            return null;
        }
        Boolean value = Boolean.valueOf(stringValue);
        return value;
    }

    public void setPreference(String key, String value) {
        if (value == null) {
            removeGlobalPreference(key);
            return;
        }
        GlobalPreference globalPreference = getGlobalPreference(key);
        if (globalPreference == null) {
            globalPreference = new GlobalPreference();
            globalPreference.setKey(key);
        }
        globalPreference.setValue(value);
        saveGlobalPreference(globalPreference);
    }

    public void setPreferenceBoolean(String key, Boolean value) {
        if (value == null) {
            removeGlobalPreference(key);
            return;
        }
        String stringValue = value.toString();
        setPreference(key, stringValue);
    }

    public GlobalPreference saveGlobalPreference(GlobalPreference globalPreference) {
        GlobalPreference managedPreference = entityManager.merge(globalPreference);
        return managedPreference;
    }

    public void removeGlobalPreference(String key) {
        GlobalPreference globalPreference = getGlobalPreference(key);
        if (globalPreference != null) {
            removeGlobalPreference(globalPreference);;
        }
    }

    public void removeGlobalPreference(GlobalPreference globalPreference) {
        GlobalPreference managedPreference = entityManager.merge(globalPreference);
        entityManager.remove(managedPreference);
    }

}
