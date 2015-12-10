/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot;
import com.cghislai.organiseurilesdepaix.domain.LocationTimeSlot_;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.search.LocationTimeSlotSearch;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 *
 * @author cghislai
 */
@Stateless
public class LocationTimeSlotService implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<LocationTimeSlot> searchLocationTimeSlots(LocationTimeSlotSearch search, Pagination pagination) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LocationTimeSlot> query = builder.createQuery(LocationTimeSlot.class);
        Root<LocationTimeSlot> locationTimeSlotRoot = query.from(LocationTimeSlot.class);

        List<Predicate> predicates = applyLocationTimeSlotSearch(search, builder, locationTimeSlotRoot);

        Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);

        query.select(locationTimeSlotRoot);
        query.where(predicatesArray);

        TypedQuery<LocationTimeSlot> typedQuery = entityManager.createQuery(query);
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        List<LocationTimeSlot> locationTimeSlots = typedQuery.getResultList();
        return locationTimeSlots;
    }

    public Long countLocationTimeSlots(LocationTimeSlotSearch search) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<LocationTimeSlot> locationTimeSlotRoot = query.from(LocationTimeSlot.class);

        List<Predicate> predicates = applyLocationTimeSlotSearch(search, builder, locationTimeSlotRoot);

        Predicate[] predicatesArray = predicates.toArray(new Predicate[0]);

        Expression<Long> locationTimeSlotCount = builder.count(locationTimeSlotRoot);
        query.select(locationTimeSlotCount);
        query.where(predicatesArray);

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        Long singleResult = typedQuery.getSingleResult();
        return singleResult;
    }

    private List<Predicate> applyLocationTimeSlotSearch(LocationTimeSlotSearch search, CriteriaBuilder builder, Root<LocationTimeSlot> locationTimeSlotRoot) {
        List<Predicate> predicates = new ArrayList<>();
        Location location = search.getLocation();
        if (location != null) {
            Path<Location> loccationPath = locationTimeSlotRoot.get(LocationTimeSlot_.location);
            Predicate locationPredicate = builder.equal(loccationPath, location);
            predicates.add(locationPredicate);
        }
        Long startHour = search.getStartHour();
        if (startHour != null) {
            Path<Long> startTimePath = locationTimeSlotRoot.get(LocationTimeSlot_.startTime);
            Predicate startHourPredicate = builder.equal(startTimePath, startHour);
            predicates.add(startHourPredicate);
        }
        CampaignDay campaignDay = search.getCampaignDay();
        if (campaignDay != null) {
            Path<CampaignDay> campaignSayPath = locationTimeSlotRoot.get(LocationTimeSlot_.campaignDay);
            Predicate campaignDayPredicate = builder.equal(campaignSayPath, campaignDay);
            predicates.add(campaignDayPredicate);
        }
        return predicates;
    }

    public LocationTimeSlot saveLocationTimeSlot(LocationTimeSlot locationTimeSlot) {
        LocationTimeSlot mergedLocationTimeSlot = entityManager.merge(locationTimeSlot);
        return mergedLocationTimeSlot;
    }

    public void deleteLocationTimeSlot(LocationTimeSlot locationTimeSlot) {
        LocationTimeSlot mergedLocationTimeSlot = entityManager.merge(locationTimeSlot);
        entityManager.remove(mergedLocationTimeSlot);
    }
}
