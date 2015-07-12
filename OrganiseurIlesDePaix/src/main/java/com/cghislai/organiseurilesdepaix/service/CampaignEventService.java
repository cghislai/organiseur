/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cghislai.organiseurilesdepaix.service;

import com.cghislai.organiseurilesdepaix.domain.CampaignEvent;
import com.cghislai.organiseurilesdepaix.domain.CampaignEvent_;
import com.cghislai.organiseurilesdepaix.domain.CampaignDay;
import com.cghislai.organiseurilesdepaix.domain.Location;
import com.cghislai.organiseurilesdepaix.domain.User;
import com.cghislai.organiseurilesdepaix.domain.util.Pagination;
import com.cghislai.organiseurilesdepaix.service.search.CampaignEventSearch;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
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
public class CampaignEventService implements Serializable {

    @PersistenceContext
    private EntityManager entityManager;

    public List<CampaignEvent> findCampaignEvents(CampaignEventSearch campaignEventSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CampaignEvent> query = builder.createQuery(CampaignEvent.class);
        Root<CampaignEvent> root = query.from(CampaignEvent.class);

        List<Predicate> predicates = applyCampaignEventSearch(campaignEventSearch, root, builder);

        query.select(root);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<CampaignEvent> typedQuery = entityManager.createQuery(query);
        Pagination pagination = campaignEventSearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }
    public void deleteAllCampaignEvents() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaDelete<CampaignEvent> delete = builder.createCriteriaDelete(CampaignEvent.class);
        
        Root<CampaignEvent> root = delete.from(CampaignEvent.class);
        

        Query query = entityManager.createQuery(delete);
        query.executeUpdate();
    }

    public List<CampaignDay> findCampaignEventsDays(CampaignEventSearch campaignEventSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<CampaignDay> query = builder.createQuery(CampaignDay.class);
        Root<CampaignEvent> root = query.from(CampaignEvent.class);

        List<Predicate> predicates = applyCampaignEventSearch(campaignEventSearch, root, builder);

        Path<CampaignDay> campaignDayPath = root.get(CampaignEvent_.campaignDay);
        query.select(campaignDayPath);
        query.distinct(true);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<CampaignDay> typedQuery = entityManager.createQuery(query);
        Pagination pagination = campaignEventSearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public List<Location> findCampaignEventsLocations(CampaignEventSearch campaignEventSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Location> query = builder.createQuery(Location.class);
        Root<CampaignEvent> root = query.from(CampaignEvent.class);

        List<Predicate> predicates = applyCampaignEventSearch(campaignEventSearch, root, builder);

        Path<Location> locationPath = root.get(CampaignEvent_.location);
        query.select(locationPath);
        query.distinct(true);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Location> typedQuery = entityManager.createQuery(query);
        Pagination pagination = campaignEventSearch.getPagination();
        if (pagination != null) {
            typedQuery.setFirstResult(pagination.getFirstIndex());
            typedQuery.setMaxResults(pagination.getPageSize());
        }
        return typedQuery.getResultList();
    }

    public Long countCampaignEvents(CampaignEventSearch campaignEventSearch) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<CampaignEvent> root = query.from(CampaignEvent.class);

        List<Predicate> predicates = applyCampaignEventSearch(campaignEventSearch, root, builder);

        Expression<Long> count = builder.count(root);
        query.select(count);
        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    public Long countUsersWithAvailabilites() {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<CampaignEvent> root = query.from(CampaignEvent.class);

        Path<User> userPath = root.get(CampaignEvent_.user);
        Expression<Long> userCount = builder.countDistinct(userPath);

        query.select(userCount);
        query.distinct(true);

        TypedQuery<Long> typedQuery = entityManager.createQuery(query);
        return typedQuery.getSingleResult();
    }

    private List<Predicate> applyCampaignEventSearch(CampaignEventSearch campaignEventSearch, Root<CampaignEvent> root, CriteriaBuilder builder) {
        List<Predicate> predicates = new ArrayList<>();
        CampaignDay campaignDay = campaignEventSearch.getCampaignDay();
        if (campaignDay != null) {
            Path<CampaignDay> campaignDayPath = root.get(CampaignEvent_.campaignDay);
            Predicate pred = builder.equal(campaignDayPath, campaignDay);
            predicates.add(pred);
        }
        Date endTime = campaignEventSearch.getEndTime();
        if (endTime != null) {
            Path<Date> endPath = root.get(CampaignEvent_.endTime);
            Predicate pred = builder.greaterThan(endPath, endTime);
            predicates.add(pred);
        }
        Date forTime = campaignEventSearch.getForTime();
        if (forTime != null) {
            Path<Date> startTimePath = root.get(CampaignEvent_.startTime);
            Path<Date> endTimePath = root.get(CampaignEvent_.endTime);
            Predicate endGreater = builder.greaterThan(endTimePath, forTime);
            Predicate startSmaller = builder.lessThanOrEqualTo(startTimePath, forTime);
            Predicate pred = builder.and(endGreater, startSmaller);
            predicates.add(pred);
        }
        Location location = campaignEventSearch.getLocation();
        if (location != null) {
            Path<Location> locationPath = root.get(CampaignEvent_.location);
            Predicate pred = builder.equal(locationPath, location);
            predicates.add(pred);
        }
        Date startTime = campaignEventSearch.getStartTime();
        if (startTime != null) {
            Path<Date> startTimePath = root.get(CampaignEvent_.startTime);
            Predicate pred = builder.lessThanOrEqualTo(startTimePath, startTime);
            predicates.add(pred);
        }
        User user = campaignEventSearch.getUser();
        if (user != null) {
            Path<User> userPath = root.get(CampaignEvent_.user);
            Predicate pred = builder.equal(userPath, user);
            predicates.add(pred);
        }
        return predicates;
    }

    public CampaignEvent saveCampaignEvent(CampaignEvent campaignEvent) {
        CampaignEvent managedCampaignEvent = entityManager.merge(campaignEvent);
        return managedCampaignEvent;
    }

    public void removeCampaignEvent(CampaignEvent campaignEvent) {
        CampaignEvent managedCampaignEvent = entityManager.merge(campaignEvent);
        entityManager.remove(managedCampaignEvent);
    }
}
